package di

import controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickApi, SlickComponents}
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.{Application, BuiltInComponentsFromContext, LoggerConfigurator, ApplicationLoader => PlayApplicationLoader}
import play.filters.HttpFiltersComponents
import service.GitService
import slick.basic.{BasicProfile, DatabaseConfig}
import util.{GenericGitHandler, GitHandlerPipeline}
import util.util.handler.GitHubHandler

import scala.concurrent.ExecutionContext
import scala.reflect.io.File

class ApplicationLoader extends PlayApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    new ApplicationComponents(context).application
  }
}

class ApplicationComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    with SlickComponents
    with DBComponents
    with EvolutionsComponents
    with HikariCPComponents
    with HttpFiltersComponents
    with PersistenceComponents {

  // TODO: Personal Preference, i prefer to have Flyway + Slick Code-gen running externally to this application
  applicationEvolutions

  lazy val projectController = new controllers.ProjectController(controllerComponents, gitService)
  lazy val rootController = new controllers.RootController(controllerComponents, gitService)
  lazy val gitService = new GitService(project, commit, gitPipeline)

  lazy val router = new _root_.router.Routes(httpErrorHandler, projectController, rootController, assets)

  lazy val gitPipeline = new GitHandlerPipeline().withHandler(new GitHubHandler).withHandler(new GenericGitHandler())
}

trait PersistenceComponents {

  implicit def executionContext: ExecutionContext

  def slickApi: SlickApi

  lazy val defaultDbProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = slickApi.dbConfig[P](DbName("default"))
  }

  lazy val project = new models.ProjectEntity(defaultDbProvider)
  lazy val commit = new models.CommitEntity(defaultDbProvider)

}
