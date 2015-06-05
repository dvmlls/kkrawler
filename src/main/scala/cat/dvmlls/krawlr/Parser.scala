package cat.dvmlls.krawlr

import akka.actor.Actor
import org.jsoup.Jsoup

object Parser {

  case class Style(body:String, base:String)
  case class HTML(body:String, base:String)
  case class Response(url:String)
  case object ResponseComplete

  def findLinks(body:String, base:String):Iterator[String] = {
    import scala.collection.JavaConversions._
    val parsed = Jsoup.parse(body, base)
    parsed.select("a[href]").map(_.absUrl("href")).toIterator ++
      parsed.select("link[href]").map(_.absUrl("href")).toIterator ++
      parsed.select("img[src]").map(_.absUrl("src"))
  }
}

class Parser extends Actor {
  import Parser._

  val receive:Receive = {
    case HTML(body, base) =>
      findLinks(body, base).foreach(sender() ! Response(_))
      sender() ! ResponseComplete
    case Style(body, base) =>
      CSS.captures(body).map(URLs.resolve(_, base)).foreach(sender() ! Response(_))
      sender() ! ResponseComplete
  }
}
