package cat.dvmlls.krawlr

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import cat.dvmlls.krawlr.Client._
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import scala.concurrent.duration._
import scala.language.postfixOps

object TestClient {
  def testRedirect(from:String, to:String)(implicit system: ActorSystem) = {
    val probe = TestProbe()
    val client = system.actorOf(Props[Client])

    probe.send(client, Request(from))
    probe.expectMsg(Redirect(to))
  }
}

class TestClient extends TestKit(ActorSystem("TestClient")) with FunSuiteLike with BeforeAndAfterAll with Matchers with ImplicitSender {

  import TestClient._
  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("redirect: root") { testRedirect("http://digitalocean.com", "https://www.digitalocean.com/")  }
  test("redirect: subdir no slash") { testRedirect("https://www.digitalocean.com/pricing", "http://www.digitalocean.com/pricing/")  }
  test("redirect: http -> https") { testRedirect("http://www.digitalocean.com/pricing/", "https://www.digitalocean.com/pricing/")  }

  import scala.reflect._
  def expectClass[T:ClassTag](url:String): Unit ={
    val probe = TestProbe()
    val client = system.actorOf(Props[Client])

    probe.send(client, Request(url))
    probe.expectMsgClass(classTag[T].runtimeClass)
  }

  test("html") { expectClass[HTML]("https://www.digitalocean.com/") }
  test("css") { expectClass[Style]("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css") }
  test("png") { expectClass[Other]("http://i.imgur.com/B9YOMTg.jpg") }
  test("not found") { expectClass[Error]("https://www.digitalocean.com/helloooooooooooooooooooooooooooooooooooo") }

  test("invalid domain") {

    implicit val timeout = Timeout(1 seconds)

    val probe = TestProbe()
    val client = system.actorOf(Props[Client], "client")
    probe.watch(client)
    probe.send(client, Request("http://www.google.com:81"))

    within(2 seconds) {
      expectNoMsg()
    }
  }
}
