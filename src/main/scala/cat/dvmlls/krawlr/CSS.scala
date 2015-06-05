package cat.dvmlls.krawlr

object CSS {
  val pattern = """url[(]\s*['"]?([^)('"\s]+)['"]?\s*[)]""".r
  def captures(source:CharSequence) = pattern.findAllMatchIn(source).flatMap(_.subgroups).toList
}
