package controllers

import api.RepositoryUrl
import models.{CommitRepository, CommitRow}
import play.api.libs.json.Json
import play.api.mvc._
import rules.{GitHub, JGit}

import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
class RepositoryController(cc: ControllerComponents,
                           commitRepository: CommitRepository) extends AbstractController(cc) {

  implicit lazy val ec = cc.executionContext

  def list() = Action.async { implicit request: Request[AnyContent] =>
    commitRepository.listRepositories.map { repositories =>
      Ok(views.html.repository.list(repositories))
    }
  }

  def index(url: RepositoryUrl) = Action.async { implicit request: Request[AnyContent] =>
    new GitHub(url).listCommits.flatMap { cs =>
      new JGit(url).listCommits.flatMap { commits =>
        commitRepository.listCommits.flatMap { existingCommits =>
          val newCommits = commits.filterNot(commit => existingCommits.exists(_.uuid == commit.uuid))
          commitRepository.create(newCommits.map(c => CommitRow(url.value, c.uuid, c.message)))
            .map { _ =>
              Ok(views.html.repository.index(commits))
            }
        }
      }
    }
  }

  def add() = Action { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded
      .flatMap(_.get("url").flatMap(_.headOption)).map(RepositoryUrl.apply)
      .fold {
        BadRequest("Invalid GitHub URL")
      } { url =>
        Redirect(routes.RepositoryController.index(url))
      }
  }

  def _commits(url: RepositoryUrl) = Action.async { implicit request: Request[AnyContent] =>
    commitRepository.listCommits(url).map { commits =>
      Ok(Json.toJson(commits).toString)
    }
  }

}
