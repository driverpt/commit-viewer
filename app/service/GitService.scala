package service

import api.{Commit, Project, RepositoryUrl}
import di.binders.RepositoryUrl
import exceptions.GitException
import models.{CommitEntity, CommitRow, ProjectEntity, ProjectRow}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.io.Directory
import scala.sys.process._

class GitService(projectEntity: ProjectEntity, commitEntity: CommitEntity) {

  def getAllProjects: Seq[Project] = {

    val projects = Await.result(projectEntity.listAll(), Duration.Inf)

    projects.map { row => Project(RepositoryUrl(row.projectId), None, "") }
  }

  def cloneRepository(url: RepositoryUrl): Option[Project] = {
    val tempDirectory = Directory.makeTemp()
    val result = s"git clone ${url.value} ${tempDirectory.path}/" !

    if (result != 0) {
      throw new RuntimeException("Something went wrong")
    }

    Some(Project(url, Some(tempDirectory.toFile), ""))
  }

  def getCommitList(repository: Project): Seq[Commit] = {
    if (repository.localLocation.isEmpty) {
      throw new IllegalArgumentException()
    }

    // Please don't do this in production!
    val projectRow = Await.result(projectEntity.get(repository.url.value), Duration.Inf)

    if (projectRow.isEmpty) {
      try {
        val result = Seq("git", "--git-dir", s"${repository.localLocation.get.path}/.git", "log", "--format=%h %at %s", "--no-decorate") !!
        val commits = result
          .split("\n")
          .map {
            _.split(" ", 3) match {
              case Array(hash, timestamp, message) => Commit(hash, message, timestamp.toInt)
            }
          }

        val projectId = repository.url.value

        // I'd rather have this saved into a variable for better readability
        var error = Await.result(projectEntity.create(List(ProjectRow(projectId))), Duration.Inf).isEmpty
        if (error) {
          throw new RuntimeException
        }

        val commitRows = commits.map { commit => CommitRow(projectId, commit.uuid, commit.message, commit.timestamp) }

        error = Await.result(commitEntity.create(commitRows.toList), Duration.Inf).isEmpty
        if (error) {
          // TODO: Create a Database Exception, i don't like Generic Exceptions
          throw new RuntimeException
        }

        return commits
      } catch {
        // Rethrow Exception to up in case something went wrong with git
        case e: RuntimeException => throw new GitException(e)
      }
    }

    // TODO: Make this fully reactive by returning Future (Not doing that to avoid Callback Hell)
    val commits = Await.result(commitEntity.getCommitsForProject(repository.url.value.toLowerCase), Duration.Inf)
    commits.map { row => Commit(row.commitId, row.commitMessage, row.timestamp) }
  }
}
