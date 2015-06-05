import java.net.URI
val u = new URI("https://www.example.com:8080")

u.getScheme
u.getHost
u.getAuthority
u.getPath
u.getQuery
u.getFragment
u.toString

val t = scala.util.Try { new URI("http://hello.com")}

t.filter(_ => false)