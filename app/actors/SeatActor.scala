package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._
import actors.messages.OutboundMessage
import actors.messages.JsonMapper

class SeatActor(gameActor: ActorRef, out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    out ! Json.obj("type"->"registering")
  }
  
  def receive = LoggingReceive {
    case js: JsValue => {
      gameActor ! JsonMapper.mapJsToMsg(js)
      out ! Json.obj("type"->"received")
    }
    case msg: OutboundMessage => out ! JsonMapper.mapMsgToJs(msg)
    case other => log.error("unhandled: " + other)
  }
}

object SeatActor {
  def props(gameActor: ActorRef)(out: ActorRef) = Props(new SeatActor(gameActor, out))
}


