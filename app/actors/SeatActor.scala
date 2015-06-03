package actors

import akka.actor.Actor
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.JsValue
import akka.actor.ActorLogging
import play.api.libs.json.Json
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess

class SeatActor(gameActor: ActorRef, out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    gameActor ! SubscriptionIMsg
    out ! Json.obj("type"->"registering")
  }
  
  def receive = LoggingReceive {
    case js: JsValue => {
      log.info("RECEIVED STH")
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

object JsonMapper {
  private def takeType(js: JsValue) = (js \ "type").validate[String]
  def mapJsToMsg(js: JsValue): InboudMessage = takeType(js) match {
    case JsError(_) => UnknownIMsg()
    case JsSuccess("chat",_) => ChatMessageIMsg((js \ "msg").as[String])
    case JsSuccess("ready",_) => ReadyForGameIMsg(BoardConfiguration())
    case JsSuccess("move",_) => MakeMoveIMsg(Move())
    case JsSuccess("ask",_) => AskForGameStateIMsg()
    case _ => UnknownIMsg()
  }
  def mapMsgToJs(msg: OutboundMessage): JsValue = msg match {
    case ChatMessageOMsg(msg, player) => Json.obj("type"->"chat", "player"->"player", "msg"->msg)
    case YourMoveOMsg() => Json.obj("type"->"yourmove")
    case NotYourMoveOMsg() => Json.obj("type"->"notyourmove")
    case MoveResultOMsg(Position(x,y),direction,success,PlayerId(player)) => Json.obj("type"->"moved","from"->Json.obj("x"->x,"y"->y),"success"->success,"player"->player)
    case CurrentStateOMsg() => Json.obj("type"->"state")
    case GameEndedOMsg(PlayerId(winner)) => Json.obj("type"->"ended","winner"->winner)
    case TechnicalMessageOMsg(msg) => Json.obj("type"->"technical","msg"->msg)
    case other => Json.obj("type"->"unknown")
  }
}
