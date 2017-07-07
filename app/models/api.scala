package object api {

  final case class Commit(uuid: String, message: String) {
    def short: String = uuid.take(7)
  }

  final case class RepositoryUrl(value: String) extends AnyVal

}
