package service

import api.{Commit, Project, RepositoryUrl}
import di.binders.RepositoryUrl
import models.{CommitEntity, CommitRow, ProjectEntity, ProjectRow}
import util.GitHandlerPipeline

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.io.Directory
import scala.sys.process._

class GitService(projectEntity: ProjectEntity, commitEntity: CommitEntity, gitHandlerPipeline: GitHandlerPipeline) {

  def getAllProjects: Seq[Project] = {
    val projects = Await.result(projectEntity.listAll(), Duration.Inf)

    projects.map { row => Project(RepositoryUrl(row.projectId), None, "") }
  }

  @Deprecated
  def cloneRepository(url: RepositoryUrl): Option[Project] = {
    val tempDirectory = Directory.makeTemp()
    val result = s"git clone ${url.value} ${tempDirectory.path}/" !

    if (result != 0) {
      throw new RuntimeException("Something went wrong")
    }

    Some(Project(url, Some(tempDirectory.toFile), ""))
  }

  def getCommitList(repository: Project): Seq[Commit] = {
    // Please don't do this in production!
    val projectRow = Await.result(projectEntity.get(repository.url.value), Duration.Inf)
    if (projectRow.isEmpty) {
      val commits = gitHandlerPipeline.execute(repository.url)
      val rows = commits.map { commit => CommitRow(repository.url.value, commit.uuid, commit.message, commit.timestamp) }

      // Ensure foreign key exists
      Await.result(projectEntity.create(List(ProjectRow(repository.url.value))), Duration.Inf)
      commitEntity.create(rows.toList)

      return commits
    }

    val commits = Await.result(commitEntity.getCommitsForProject(repository.url.value), Duration.Inf)
    commits.map { row => Commit(row.commitId, row.commitMessage, row.timestamp) }
  }
}
