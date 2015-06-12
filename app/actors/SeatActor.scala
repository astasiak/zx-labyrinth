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
import game.Board
import game.North
import game.PlayerId
import game.PlayerA
import game.GameState

class SeatActor(gameActor: ActorRef, out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
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
    case JsSuccess("sit",_) => SubscriptionIMsg((js \ "name").as[String])
    case JsSuccess("chat",_) => ChatMessageIMsg((js \ "msg").as[String])
    case JsSuccess("ready",_) => InitBoardIMsg(Board((0,0),(0,0),(0,0),List()))
    case JsSuccess("move",_) => MakeMoveIMsg(North)
    case JsSuccess("ask",_) => AskForGameStateIMsg()
    case _ => UnknownIMsg()
  }
  def mapMsgToJs(msg: OutboundMessage): JsValue = msg match {
    case ChatMessageOMsg(player, msg) => Json.obj("type"->"chat", "player"->player, "msg"->msg)
    case TechnicalMessageOMsg(msg) => Json.obj("type"->"technical","msg"->msg)
    case SitDownSuccessOMsg(playerId) => Json.obj("type"->"sit_ok","player"->mapPlayerId(playerId))
    case SitDownFailOMsg() => Json.obj("type"->"sit_fail")
    case UpdateBoardOMsg(player, board) => Json.obj("type"->"update_board","board"->mapBoard(board))
    case UpdatePlayersOMsg(playerA, playerB) => Json.obj("type"->"update_players","a"->mapPlayer(playerA),"b"->mapPlayer(playerB))
    case UpdateStateOMsg(gameState) => Json.obj("type"->"update_state","state"->mapState(gameState))
    case other => Json.obj("type"->"unknown")
  }
  def mapPlayerId(playerId: PlayerId) = if(playerId==PlayerA) "A" else "B"
  def mapBoard(board: Board) = board.toString
  def mapPlayer(player: Option[String]) = player.toString
  def mapState(gameState: GameState) = gameState.toString
}
