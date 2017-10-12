package controllers

import java.net.URI

import api.Project
import di.binders.RepositoryUrl
import play.api.mvc._
import service.GitService

import scala.concurrent.{ExecutionContext, Future}

class ProjectController(cc: ControllerComponents,
                        gitService: GitService) extends AbstractController(cc) {

  implicit lazy val ec: ExecutionContext = cc.executionContext

  def index(repositoryUrl: RepositoryUrl): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Future {
      val uriObj = URI.create(repositoryUrl.value)
      if (!uriObj.getHost.equalsIgnoreCase("github.com")) {
        BadRequest("Invalid GitHub URL")
      }

      val commitList = gitService.getCommitList(Project(repositoryUrl, None, ""))

      Ok(views.html.project.index(repositoryUrl, commitList))
    }
  }
}
