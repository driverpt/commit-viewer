package controllers

import java.net.URI

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

      // We should add try/catch to check if repository does not exist and show a beautiful "Not found" page
      val repo = gitService.cloneRepository(repositoryUrl)
      if (repo.isEmpty) {
        BadRequest("Invalid Git Repo")
      }

      val commitList = gitService.getCommitList(repo.get)

      // Since this is no longer needed, let's just delete the cloned repo
      repo.get.localLocation.deleteRecursively()

      Ok(views.html.project.index(repositoryUrl, commitList))
    }
  }
}
