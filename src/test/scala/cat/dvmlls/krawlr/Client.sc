import com.ning.http.client.AsyncHttpClient

val c = new AsyncHttpClient()

val urls = List("https://www.digitalocean.notadomain")

urls.map(url => {
  url -> c.prepareGet(url).execute().get().getContentType
}).foreach(println)

