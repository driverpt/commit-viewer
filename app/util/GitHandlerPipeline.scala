package util

import api.{Commit, Project}
import di.binders.RepositoryUrl
import exceptions.GitException

import scala.util.control.Breaks._
import scala.collection.mutable
import scala.reflect.io.Directory
import scala.sys.process._

trait GitHandler {
  def handle(url: RepositoryUrl): Seq[Commit]

  def canHandle(url: RepositoryUrl): Boolean
}

class GitHandlerPipeline {
  val pipeline: mutable.ListBuffer[GitHandler] = mutable.ListBuffer()

  def withHandler(handler: GitHandler): GitHandlerPipeline = {
    pipeline.append(handler)
    this
  }



  def execute(url: RepositoryUrl): Seq[Commit] = {
    var result: Seq[Commit] = Seq.empty
    pipeline.foreach {
      pipe =>
        if (pipe.canHandle(url)) {
          try {
            val handlerResult = pipe.handle(url)
            result = handlerResult
            break
          } catch {
            case _: Throwable => // Do nothing for now
          }
        }
    }
    result
  }
}

class GenericGitHandler extends GitHandler {
  def cloneRepository(url: RepositoryUrl): Option[Project] = {
    val tempDirectory = Directory.makeTemp()
    val result = s"git clone ${url.value} ${tempDirectory.path}/" !

    if (result != 0) {
      None
    }

    Some(Project(url, Some(tempDirectory.toFile), ""))
  }

  override def handle(url: RepositoryUrl): Seq[Commit] = {
    val project = cloneRepository(url)

    if (project.isEmpty) {
      throw new RuntimeException("Something went wrong when cloning")
    }

    val repository = project.get

    try {
      val result = Seq("git", "--git-dir", s"${repository.localLocation.get.path}/.git", "log", "--format=%h %at %s", "--no-decorate") !!
      val commits = result
        .split("\n")
        .map {
          _.split(" ", 3) match {
            case Array(hash, timestamp, message) => Commit(hash, message, timestamp.toInt)
          }
        }

      if (project.get.localLocation.isDefined) {
        project.get.localLocation.get.deleteRecursively()
      }
      commits.toSeq
    } catch {
      // Rethrow Exception to up in case something went wrong with git
      case e: RuntimeException => throw new GitException(e)
    }
  }

  override def canHandle(url: RepositoryUrl) = true
}