package cat.dvmlls.krawlr

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.ning.http.client.AsyncHttpClient

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Try

object Client {

  case class Request(url:String)

  sealed trait Response
  case class HTML(body:String) extends Response
  case class Style(body:String) extends Response
  case class Other() extends Response
  case class Redirect(url:String) extends Response
  case class Error(message:String) extends Response

  def request(url:String, client:AsyncHttpClient)(implicit ctx:ExecutionContextExecutor):Future[Response] = {
    val future = client.prepareGet(url).execute()
    val promise = Promise[Response]()
    future.addListener(new Runnable {
      override def run(): Unit = {
        Try { future.get() } match {
          case scala.util.Success(response) =>
            response.getStatusCode match {
              case 200 => promise.success(response.getContentType match {
                case "text/html" => HTML(response.getResponseBody)
                case "text/css" => Style(response.getResponseBody)
                case _ => Other()
              })
              case 301 => promise.success(Redirect(response.getHeader("Location")))
              case _ => promise.success(Error(response.getStatusText))
            }
          case scala.util.Failure(ex) => promise.failure(ex)
        }
      }
    }, ctx)

    promise.future
  }
}

class Client extends Actor with ActorLogging {
  import Client._
  implicit val ctx = context.dispatcher

  val client = new AsyncHttpClient()

  val receive:Receive = {
    case Request(url) => request(url, client).pipeTo(sender())
  }

  override def postStop(): Unit = {
    log.info("shutting down http client")
    client.close()
  }
}
