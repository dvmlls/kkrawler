package cat.dvmlls.krawlr

import java.net.URI

import scala.util.Try

object URLs {

  def resolve(url:String, base:String) = {
    val addition = new URI(url)

    if (addition.isAbsolute) addition.toString
    else {
      val root = new URI(if (base.endsWith("/")) base else base + "/")
      root.resolve(addition).toString
    }
  }

  def validate(url:String, domain:String) = Try { new URI(url) }
    .filter(_.getHost == domain)
    .filter(uri => uri.getScheme == "http" || uri.getScheme == "https")
    .map(uri => new URI(uri.getScheme + "://" + uri.getAuthority + uri.getPath + Option(uri.getQuery).getOrElse(""))) // remove fragment
}
