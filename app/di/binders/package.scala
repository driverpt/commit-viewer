package di

import java.nio.charset.Charset

import play.api.mvc.PathBindable
import play.utils.UriEncoding

package object binders {

  type RepositoryUrl = api.RepositoryUrl

  implicit val RepositoryUrlBinding: PathBindable[RepositoryUrl] = new PathBindable[RepositoryUrl] {
    private val stringBinder = implicitly[PathBindable[String]]

    def bind(key: String, value: String): Either[String, RepositoryUrl] =
      stringBinder.bind(key, value).right.map(api.RepositoryUrl.apply)

    def unbind(key: String, id: RepositoryUrl): String = UriEncoding.encodePathSegment(stringBinder.unbind(key, id.value), Charset.defaultCharset())
  }

}
