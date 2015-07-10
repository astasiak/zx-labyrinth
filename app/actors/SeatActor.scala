package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._

import actors.messages._
import dao.MongoUserDao

class SeatActor(gameActor: ActorRef, user: String, out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    context watch out
    out ! Json.obj("type"->"registering")
    gameActor ! AskForParamsIMsg()
    gameActor ! SubscriptionIMsg(user)
    MongoUserDao.touchUser(user)
  }
  
  def receive = LoggingReceive {
    case js: JsValue => {
      gameActor ! JsonMapper.mapJsToMsg(js)
      out ! Json.obj("type"->"received")
      MongoUserDao.touchUser(user)
    }
    case Terminated(sender) => if(sender==out) context stop self
    case msg: OutboundMessage => out ! JsonMapper.mapMsgToJs(msg)
    case other => log.error("unhandled: " + other)
  }
}

object SeatActor {
  def props(gameActor: ActorRef, user: String)(out: ActorRef) = Props(new SeatActor(gameActor, user, out))
}


