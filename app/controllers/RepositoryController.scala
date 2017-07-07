package controllers

import api.RepositoryUrl
import models.FooBar
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
class RepositoryController(cc: ControllerComponents,
                           fooBar: FooBar) extends AbstractController(cc) {

  implicit lazy val ec = cc.executionContext

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def list() = Action.async { implicit request: Request[AnyContent] =>
    ???
  }

  def index(url: RepositoryUrl) = Action.async { implicit request: Request[AnyContent] =>
    ???
  }

  def add() = Action { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded
      .flatMap(_.get("url").flatMap(_.headOption)).map(RepositoryUrl)
      .fold {
        BadRequest("Invalid GitHub URL")
      } { url =>
        ???
        Redirect(routes.RepositoryController.index(url))
      }
  }

}
