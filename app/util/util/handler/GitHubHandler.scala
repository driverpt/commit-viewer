package util.util.handler

import java.net.URI

import api.Commit
import di.binders.RepositoryUrl
import exceptions.GitException
import rules.GitHub
import util.GitHandler

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// I know that this is wrong, i just put it here to speed things up
import scala.concurrent.ExecutionContext.Implicits.global

class GitHubHandler extends GitHandler {
  override def handle(url: RepositoryUrl): Seq[Commit] = {
    val github = new GitHub(url)
    try {
      val commits = Await.result(github.listCommits, Duration.Inf)
      return commits
    } catch {
      case e: RuntimeException => throw new GitException(e)
    }

    Seq.empty
  }

  override def canHandle(url: RepositoryUrl): Boolean = {
    try {
      val uri = URI.create(url.value)
      return uri.getHost.equalsIgnoreCase("github.com")
    } catch {
      case _: Throwable =>
    }
    false
  }
}
