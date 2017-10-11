package controllers

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import api.RepositoryUrl
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class RootController(cc: ControllerComponents) extends AbstractController(cc) {

  implicit lazy val ec: ExecutionContext = cc.executionContext

  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Future {
      Ok(views.html.root.index())
    }
  }

  def handleRepo(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Future {
      request.body.asFormUrlEncoded
        .flatMap(_.get("repositoryUrl").flatMap(_.headOption)).map(RepositoryUrl)
        .fold {
          BadRequest("Invalid GitHub URL")
        } { url =>
          // TODO: Wrap this call into a companion object method. Makes code more readable
          Redirect(routes.ProjectController.index(RepositoryUrl(URLEncoder.encode(url.value, StandardCharsets.US_ASCII.name()))))
        }
    }
  }
}
