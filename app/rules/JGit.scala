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

   def withRepository[A](f: File => Future[A]): Future[A] = {
    withTemporaryDirectory { directory =>

      Logger.info(s"Cloning from ${repository.value} to $directory")

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

      f(directory)
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
