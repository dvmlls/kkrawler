package cat.dvmlls.krawlr

object Dot {

  def print(m:Map[String,Set[String]], title:String):String = {
    val body = m.flatMap { case (k, vs) => vs.map(v => s"""\t"$k" -> "$v";""")}.mkString("\n")
    s"""digraph "$title" {
      |$body
      |}
    """.stripMargin
  }

}
