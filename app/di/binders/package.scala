package di

import play.api.mvc.PathBindable

package object binders {

  type RepositoryUrl = api.RepositoryUrl

  implicit val RepositoryUrlBinding: PathBindable[RepositoryUrl] = new PathBindable[RepositoryUrl] {
    private val stringBinder = implicitly[PathBindable[String]]

    def bind(key: String, value: String): Either[String, RepositoryUrl] =
      stringBinder.bind(key, value).right.map(api.RepositoryUrl)

    def unbind(key: String, id: RepositoryUrl): String =
      stringBinder.unbind(key, id.value)
  }

}
