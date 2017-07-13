package rules

import api.{Commit, RepositoryUrl}
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.service.CommitService

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class GitHub(url: RepositoryUrl)
            (implicit executionContext: ExecutionContext) {

  private val repositoryId = RepositoryId.createFromUrl(url.value)

  def listCommits: Future[Seq[Commit]] = Future {
      new CommitService()
        .getCommits(repositoryId).asScala
        .map { c => Commit(c.getSha, c.getCommit.getMessage) }
  }

}
