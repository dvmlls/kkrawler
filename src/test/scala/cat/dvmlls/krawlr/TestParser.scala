package cat.dvmlls.krawlr

import org.scalatest.{Matchers, FunSuite}

import scala.io.Source

object TestParser {
  def getFile(name:String) = Source.fromURL(getClass.getResource("/" + name)).getLines().mkString("\n")
}

class TestParser extends FunSuite with Matchers {

  import TestParser._

  test("find all links") {
    val links = Parser.findLinks(getFile("test.html"), "http://www.example.com").toList
    assert(links.length === 10)

    val desired =
      """http://www.example.com/
        |http://www.example.com
        |http://www.example.com/subdirectory
        |http://www.example.com/subdirectory
        |http://www.example.com/subdirectory
        |http://www.example.com/style.css
        |http://www.other.com/style.css
        |http://www.example.com/hello.jpg
        |http://www.example.com/subdirectory/hello.jpg
        |http://www.other.com/hello.jpg
      """.stripMargin.trim

    assert(links.mkString("\n") === desired)
  }

}
