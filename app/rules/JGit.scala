package rules

import api._
import better.files._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import play.api.Logger

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


class JGit(repository: RepositoryUrl)
          (implicit executionContext: ExecutionContext) {

  def listCommits: Future[List[Commit]] = {
    withRepository { git =>

      Future.successful {
        val commits = git
          .log()
          .call().asScala

        commits.map {
          commit =>
            Commit(commit.getId.getName, commit.getShortMessage)
        }.to[List]
      }

    }
  }

  private def withRepository[A](f: Git => Future[A]): Future[A] = {
    withTemporaryDirectory { directory =>

      Logger.info(s"Cloning from ${repository.value} to $directory")

      Future.successful {
        val git =
          Git.cloneRepository()
            .setURI(repository.value)
            .setDirectory(directory.toJava)
            .call()

        git
          .reset()
          .setMode(ResetType.HARD)
          .call()

        Logger.info(s"Cloned ${repository.value}")

        git
      }.flatMap { git =>
        f(git)
      }
    }
  }

  private def withTemporaryDirectory[A](f: File => Future[A]): Future[A] = {
    val directory = File.newTemporaryDirectory()
    directory.delete()

    f(directory).andThen { case _ =>
      directory.delete()
    }
  }

}
