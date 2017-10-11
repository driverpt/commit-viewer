package service

import api.{Commit, Repository}
import di.binders.RepositoryUrl
import play.api.Logger

import scala.reflect.io.{Directory, File}
import scala.sys.process._

class GitService(location: File) {
  def cloneRepository(url: RepositoryUrl): Option[Repository] = {
    val tempDirectory = Directory.makeTemp()
    val result = s"git clone ${url.value} ${tempDirectory.path}/" !

    if (result != 0) {
      throw new RuntimeException("Something went wrong")
    }

    Some(Repository(url, tempDirectory.toFile, ""))
  }

  def getCommitList(repository: Repository): Seq[Commit] = {
    val result = s"git --git-dir ${repository.localLocation.path}/.git log --oneline --no-decorate" !!

    val commits = result
      .split("\n")
      .map {
        _.split(" ", 2) match {
          case Array(hash, message) => Commit(hash, message)
        }
      }

    commits
  }
}
