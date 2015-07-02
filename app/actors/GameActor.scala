package actors

import akka.event.LoggingReceive
import akka.actor.Terminated
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import game.Board
import game.GameParams
import game.Game
import game.PlayerId
import game.Direction
import com.google.common.collect.HashBiMap
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import game.Callbacks
import game.GameState
import actors.messages._
import com.typesafe.scalalogging.LazyLogging

private class AkkaSeatCallbacks(seatActor: ActorRef) extends Callbacks {
  def updatePlayers(playerA: Option[String], playerB: Option[String]) = seatActor ! UpdatePlayersOMsg(playerA, playerB)
  def updateBoard(playerId: PlayerId, board: Board) = seatActor ! UpdateBoardOMsg(playerId, board)
  def updateGameState(gameState: GameState) = seatActor ! UpdateStateOMsg(gameState)
}

class GameActor(params: GameParams) extends Actor with ActorLogging {
  var game: Game = new Game(params)
  val playerMap: Map[ActorRef, (Option[PlayerId],String)] = Map()
  
  override def receive = LoggingReceive {
    case SubscriptionIMsg(playerName) => subscribe(playerName)
    case SitDownIMsg() => sitDown()
    case ChatMessageIMsg(msg) => chat(msg)
    case Terminated(seat) => unsubscribe()
    case InitBoardIMsg(board) => initBoard(board)
    case MakeMoveIMsg(move) => makeMove(move)
    case AskForParamsIMsg() => askForGameState()
    case KeepAliveIMsg() => log.debug("Connection kept alive")
    case other => log.error("unhandled: " + other)
  }
  
  private def subscribe(playerName: String) = {
    val success = game.subscribe(playerName, new AkkaSeatCallbacks(sender))
    if(success) {
      context watch sender
      playerMap += sender->(None,playerName)
      playerMap.keys foreach {
        _ ! PlayerPresenceOMsg(playerName,true)
      }
    } else {
      sender ! SitDownFailOMsg()
    }
  }
  
  private def unsubscribe() = {
    val userId = playerMap(sender)._2
    game.unsubscribe(userId)
    playerMap -= sender
    playerMap.keys foreach {
      _ ! PlayerPresenceOMsg(userId,false)
    }
  }
  
  private def sitDown() = {
    val userId = playerMap(sender)._2
    val playerId = game.sitDown(userId)
    playerId match {
      case None => sender ! SitDownFailOMsg()
      case Some(playerId) =>
        playerMap.put(sender, (Some(playerId),userId))
        sender ! SitDownSuccessOMsg(playerId)
    }
  }
  
  private def chat(msg: String) = playerMap.get(sender).map(_._2) match {
    case None => log.warning("Trying to chat while not being subscribed")
    case Some(userId) => playerMap.keys.foreach { receiver=>
      receiver ! ChatMessageOMsg(userId,msg)
    }
  }
  
  private def initBoard(board: Board) = myPlayerId() match {
    case None => log.warning("Trying to init board while not sitting")
    case Some(playerId) =>
      val success = game.initBoard(playerId, board)
      sender ! InitBoardResultOMsg(success)
  }
  
  private def makeMove(move: Direction) = myPlayerId() match {
    case None => log.warning("Trying to make move while not sitting")
    case Some(playerId) => game.declareMove(playerId, move)
  }
  
  private def askForGameState() = sender ! ParamsOMsg(params)
  
  private def myPlayerId(): Option[PlayerId] = {
    return playerMap.get(sender).flatMap(_._1)
  }
}