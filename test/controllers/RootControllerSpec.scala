package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class RootControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "RootController GET" should {
    "render the index page from a new instance of controller" in {
      val controller = new RootController(stubControllerComponents())
      val root = controller.index().apply(FakeRequest(GET, "/"))

      status(root) mustBe OK
      contentType(root) mustBe Some("text/html")

    }

  }
}
