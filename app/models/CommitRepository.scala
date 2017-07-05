package models

import api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

final case class CommitRow(url: String, uuid: String, message: String)

class CommitRepository(protected val dbConfigProvider: DatabaseConfigProvider)
                      (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getProfile: JdbcProfile = profile

  def database: JdbcBackend#DatabaseDef = db

  class CommitTable(tag: Tag) extends Table[CommitRow](tag, "commit") {
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (url, uuid, message) <> (CommitRow.tupled, CommitRow.unapply)

    def url = column[String]("url")

    def uuid = column[String]("uuid")

    def message = column[String]("message")
  }

  lazy val CommitTable = new TableQuery(tag => new CommitTable(tag))

  def create(row: CommitRow): Future[Int] =
    db.run(CommitTable += row)

  def create(row: List[CommitRow]): Future[Option[Int]] =
    db.run(CommitTable ++= row)

  def listCommits: Future[Seq[Commit]] =
    db.run(CommitTable.result)
      .map(_.map(c => Commit(c.uuid, c.message)))

  def listCommits(url: RepositoryUrl): Future[Seq[Commit]] =
    db.run(CommitTable.filter(_.url === url.value).result)
      .map(_.map(c => Commit(c.uuid, c.message)))

  def listRepositories: Future[Seq[RepositoryUrl]] =
    db.run(CommitTable.groupBy(_.url).map { case (url, _) => url }.result)
      .map(_.map(RepositoryUrl.apply))

}
