package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

final case class ProjectRow(projectId: String)

class ProjectEntity(protected val dbConfigProvider: DatabaseConfigProvider)
                   (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getProfile: JdbcProfile = profile

  def database: JdbcBackend#DatabaseDef = db

  class ProjectTable(tag: Tag) extends Table[ProjectRow](tag, "projects") {
    // Every table needs a * projection with the same type as the table's type parameter
    def * = projectId <> (ProjectRow.apply, ProjectRow.unapply)

    def projectId = column[String]("project_id")
  }

  lazy val ProjectTableQuery = new TableQuery(tag => new ProjectTable(tag))

  def create(row: List[ProjectRow]): Future[Option[Int]] =
    db.run(ProjectTableQuery ++= row)

  def get(md5: String): Future[Option[ProjectRow]] =
    db.run(ProjectTableQuery.filter(_.projectId === md5).result.map(_.headOption))

}