package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

final case class CommitRow(projectId: String, commitId: String, commitMessage: String)

class CommitEntity(protected val dbConfigProvider: DatabaseConfigProvider)
                  (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getProfile: JdbcProfile = profile

  def database: JdbcBackend#DatabaseDef = db

  class CommitTable(tag: Tag) extends Table[CommitRow](tag, "commits") {
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (projectId, commitId, commitMessage) <> (CommitRow.tupled, CommitRow.unapply)

    def projectId = column[String]("project_id")

    def commitId = column[String]("commit_id")

    def commitMessage = column[String]("commit_message")

  }

  lazy val CommitTable = new TableQuery(tag => new CommitTable(tag))

  def create(row: List[CommitRow]): Future[Option[Int]] =
    db.run(CommitTable ++= row)

  def getCommitsForProject(projectId: String): Future[Seq[CommitRow]] = {
    val filter: Query[CommitTable, CommitRow, Seq] = CommitTable.filter(_.projectId === projectId)
    db.run(filter.result)
  }
}
