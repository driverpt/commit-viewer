import play.api.libs.json.{Json, OFormat}

package object api {

  final case class Commit(uuid: String, message: String) {
    def short: String = uuid.take(7)
  }

  object Commit {
    implicit val fmt: OFormat[Commit] = Json.format[Commit]
  }

  final case class RepositoryUrl(value: String) extends AnyVal

  object RepositoryUrl {
    implicit val fmt: OFormat[RepositoryUrl] = Json.format[RepositoryUrl]
  }

}
