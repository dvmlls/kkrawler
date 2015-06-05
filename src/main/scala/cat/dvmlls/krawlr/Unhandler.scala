package cat.dvmlls.krawlr

import akka.actor.{Actor, ActorLogging, UnhandledMessage}

class Unhandler extends Actor with ActorLogging{

  context.system.eventStream.subscribe(self, classOf[UnhandledMessage])

  val receive:Receive = {
    case UnhandledMessage(m, s, r) => log.warning(s"message=$m sender=$s, recipient=$r")
  }

}
