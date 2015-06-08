package cat.dvmlls.krawlr

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.ning.http.client.{Response => NResponse, AsyncHttpClientConfig, AsyncCompletionHandler, AsyncHttpClient}

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

object Client {

  case class Request(url:String)

  sealed trait Response
  case class HTML(body:String) extends Response
  case class Style(body:String) extends Response
  case class Other() extends Response
  case class Redirect(url:String) extends Response

  def request(url:String, client:AsyncHttpClient)(implicit ctx:ExecutionContextExecutor):Future[Response] = {
    val promise = Promise[Response]()
    val builder = client.prepareGet(url)
    client.prepareGet(url).execute(new AsyncCompletionHandler[NResponse] {
      override def onCompleted(response: NResponse): NResponse = {

        response.getStatusCode match {
          case 200 => promise.success(response.getContentType match {
            case "text/html" => HTML(response.getResponseBody)
            case "text/css" => Style(response.getResponseBody)
            case _ => Other()
          })
          case 301 => promise.success(Redirect(response.getHeader("Location")))
          case _ => promise.failure(new Exception(response.getStatusText))
        }

        response
      }

      override def onThrowable(ex:Throwable) { promise.failure(ex) }
    })

    promise.future
  }
}

class Client(timeoutMS:Option[Int]=None) extends Actor with ActorLogging {
  import Client._
  implicit val ctx = context.dispatcher

  val client = {
    var builder = new AsyncHttpClientConfig.Builder()

    timeoutMS.foreach(t => builder = builder.setConnectionTimeoutInMs(t))

    new AsyncHttpClient(builder.build())
  }

  val receive:Receive = {
    case Request(url) => request(url, client).pipeTo(sender())
  }

  override def postStop(): Unit = {
    log.info("shutting down http client")
    client.close()
  }
}
