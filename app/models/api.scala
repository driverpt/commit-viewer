import scala.reflect.io.File

package object api {

  final case class Commit(uuid: String, message: String) {
    def short: String = uuid.take(7)
  }

  final case class RepositoryUrl(value: String) extends AnyVal

  final case class Repository(url: RepositoryUrl, localLocation: File, name: String)
}
