package cat.dvmlls.krawlr

import java.io.{FileWriter, BufferedWriter, File}

import akka.actor._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import akka.actor.SupervisorStrategy.Restart

object Main extends App {

  val s = ActorSystem("krawlr")
  implicit val timeout = Timeout(5 seconds)
  implicit val ctx = s.dispatcher

  val c = s.actorOf(Props[Client], "client")
  val p = s.actorOf(Props[Parser], "parser")
  s.actorOf(Props[Unhandler], "unhandler")

  class Worker(url:String) extends Actor with ActorLogging {
    c ! Client.Request(url)

    context.system.scheduler.scheduleOnce(30 seconds, self, Kill)

    def stop(): Unit = {
      context.parent ! Complete
      context.stop(self)
    }

    val awaitingLinks:Receive = {
      case Parser.Response(to) => context.parent ! Reference(url, to)
      case Parser.ResponseComplete => stop()
    }

    val receive:Receive = {
      case Client.HTML(body) =>
        p ! Parser.HTML(body, url)
        context.become(awaitingLinks)
      case Client.Style(body) =>
        p ! Parser.Style(body, url)
        context.become(awaitingLinks)
      case Client.Redirect(to) =>
        context.parent ! Redirect(url, to)
        stop()
      case Client.Error(msg) =>
        log.error(s"msg=$msg url=$url")
        context.parent ! Broken(url)
        stop()
      case Client.Other() => stop()
      case Failure(ex) =>
        log.warning("killing myself", ex)
        throw new Exception("restarting myself", ex)
    }
  }

  case object Complete
  case class Redirect(from:String, to:String)
  case class Reference(from:String, to:String)
  case class Broken(url:String)

  class Main(domain:String) extends Actor with ActorLogging {

    var checked = Set.empty[String]
    var workers = Set.empty[ActorRef]

    var redirects = Map.empty[String,String]
    var references = Map.empty[String,Set[String]]
    var broken = Set.empty[String]

    log.info(s"running for domain: $domain")
    check(s"http://$domain")

    override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, loggingEnabled=true) {
      case _ =>
        log.info("restarting: " + sender())
        Restart
    }

    def check(url:String): Unit = {
      if (checked.contains(url)) return
      checked += url

      workers += context.actorOf(Props{ new Worker(url) })
    }

    object Validated {
      def unapply(url:String):Option[String] = {
        URLs.validate(url, domain) match {
          case Success(result) => Some(result.toString)
          case Failure(ex) =>
            log.debug(s"URL parse failure: url=$url ex=$ex")
            None
        }
      }
    }

    def trim(url:String) = url.replaceAll(s"http[s]?[:][/][/]$domain", "")

    val receive:Receive = {
      case Broken(url) => broken += url
      case Redirect(from, Validated(to)) =>
        redirects += from -> to
        check(to)
      case Reference(from, Validated(to)) =>
        val tf = trim(from)
        val tt = trim(to)
        references += tf -> (references.getOrElse(tf, Set.empty[String]) + tt)
        check(to)
      case Complete =>
        workers -= sender()
        if (workers.size < 10 || workers.size % 10 == 0) log.info(s"workers: ${workers.size}")
        if (workers.size == 0) {
          log.info("finished")
          val file = new File("output.gv")
          val w = new BufferedWriter(new FileWriter(file))
          w.write(Dot.print(references, domain))
          w.close()
          context.system.shutdown()
        }
    }
  }

  val main = s.actorOf(Props { new Main(args(0)) } , "main")
}
