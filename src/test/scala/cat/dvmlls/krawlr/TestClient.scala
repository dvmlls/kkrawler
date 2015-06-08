package cat.dvmlls.krawlr

import akka.actor.Status.Failure
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import cat.dvmlls.krawlr.Client._
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import scala.concurrent.duration._
import scala.language.postfixOps

object TestClient {
  def testRedirect(from:String, to:String)(implicit system: ActorSystem) = {
    val probe = TestProbe()
    val client = system.actorOf(Props { new Client() } )

    probe.send(client, Request(from))
    probe.expectMsg(Redirect(to))
  }
}

class TestClient extends TestKit(ActorSystem("TestClient")) with FunSuiteLike with BeforeAndAfterAll with Matchers with ImplicitSender {

  import TestClient._
  override def afterAll(): Unit = {
    system.shutdown()
  }

  val d_o = "digitalocean.com"

  test("redirect: root") { testRedirect(s"http://$d_o", s"https://www.$d_o/")  }
  test("redirect: subdir no slash") { testRedirect(s"https://www.$d_o/pricing", s"http://www.$d_o/pricing/")  }
  test("redirect: http -> https") { testRedirect(s"http://www.$d_o/pricing/", s"https://www.$d_o/pricing/")  }

  import scala.reflect._
  def expectClass[T:ClassTag](url:String): Unit ={
    val probe = TestProbe()
    val client = system.actorOf(Props { new Client() } )

    probe.send(client, Request(url))
    probe.expectMsgClass(classTag[T].runtimeClass)
  }

  test("html") { expectClass[HTML](s"https://www.$d_o/") }
  test("css") { expectClass[Style]("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css") }
  test("png") { expectClass[Other]("http://i.imgur.com/B9YOMTg.jpg") }
  test("not found") { expectClass[Failure](s"https://www.$d_o/helloooooooooooooooooooooooooooooooooooo") }

  test("timeout") {
    val probe = TestProbe()
    val client = system.actorOf(Props { new Client(timeoutMS = Some(1000)) } )
    probe.watch(client)
    probe.send(client, Request("http://www.google.com:81"))

    probe.expectMsgClass(2 seconds, classOf[Failure])
  }
}
