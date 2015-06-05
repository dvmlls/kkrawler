package cat.dvmlls.krawlr

import org.scalatest.{Tag, Ignore, Matchers, FunSuite}

import scala.io.Source

class TestCSS extends FunSuite with Matchers {

  test("quotes: none") {
    val s = "blah blah url(http://example.com) blah blah"
    assert(CSS.captures(s).head === "http://example.com")
  }

  test("quotes: single") {
    val s = "blah blah url('http://example.com') blah blah"
    assert(CSS.captures(s).head === "http://example.com")
  }

  test("quotes: double") {
    val s = """blah blah url("http://example.com") blah blah"""
    assert(CSS.captures(s).head === "http://example.com")
  }

  test("whitespace") {
    val s = """blah blah url( http://example.com  ) blah blah"""
    assert(CSS.captures(s).head === "http://example.com")
  }

  test("whitespace and quotes") {
    val s = """blah blah url( "http://example.com"  ) blah blah"""
    assert(CSS.captures(s).head === "http://example.com")
  }

  test("two URLs") {
    val s = """blah blah url( "http://example.com"  ) blah blah url(http://potato.com) blah blah"""
    val captures = CSS.captures(s)
    assert(captures.head === "http://example.com")
    assert(captures.tail.head === "http://potato.com")
  }

  test("from file") {
    val s = Source.fromURL(getClass.getResource("/test.css")).getLines().mkString("\n")
    val captures = CSS.captures(s)
    assert(captures.head === "bgdesert.jpg")
  }

  ignore("escaped parentheses") {
    /* http://www.w3.org/TR/CSS21/syndata.html#uri */
    val s = """blah blah url( "http://example.com/why\(yes\)"  ) blah blah"""
    assert(CSS.captures(s).head === "http://example.com/why\\(yes\\)")
  }
}
