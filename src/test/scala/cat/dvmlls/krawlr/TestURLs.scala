package cat.dvmlls.krawlr

import org.scalatest.{Matchers, FunSuite}

import scala.util.{Failure, Success}

class TestURLs extends FunSuite with Matchers {

  val base = "http://www.example.com"
  val domain = "www.example.com"

  test("as-is") { assert(base === URLs.resolve(base, "")) }
  test("relative: no slash") { assert(s"$base/hello" === URLs.resolve("../hello", s"$base/bacon")) }
  test("relative: yes slash") { assert(s"$base/hello" === URLs.resolve("../hello", s"$base/bacon/")) }

  test("validate: http") { assert(URLs.validate("http://www.example.com", domain).isSuccess) }
  test("validate: https") { assert(URLs.validate("https://www.example.com", domain).isSuccess) }
  test("validate: https with port") { assert(URLs.validate("https://www.example.com:8080", domain).isSuccess) }
  test("validate: mailto") { assert(URLs.validate("mailto:dave@www.example.com", domain).isFailure) }
  test("validate: fragment") {
    /* http://galimatias.mola.io */
    URLs.validate("https://www.example.com:8080/#", domain) match {
      case Success(uri) => assert(uri.getFragment == null)
      case Failure(ex) => fail(ex)
    }
  }

}
