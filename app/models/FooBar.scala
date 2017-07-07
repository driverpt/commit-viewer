package models

import api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

final case class FooBarRow(foo: String, bar: String)

class FooBar(protected val dbConfigProvider: DatabaseConfigProvider)
                      (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def getProfile: JdbcProfile = profile

  def database: JdbcBackend#DatabaseDef = db

  class FooBarTable(tag: Tag) extends Table[FooBarRow](tag, "foobar") {
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (foo, bar) <> (FooBarRow.tupled, FooBarRow.unapply)

    def foo = column[String]("foo")

    def bar = column[String]("bar")

  }

  lazy val FooBarTable = new TableQuery(tag => new FooBarTable(tag))

  def create(row: List[FooBarRow]): Future[Option[Int]] =
    db.run(FooBarTable ++= row)



}
