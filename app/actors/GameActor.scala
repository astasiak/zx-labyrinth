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
import com.google.common.collect.BiMap
import game.Direction
import com.google.common.collect.HashBiMap
import scala.collection.JavaConversions._
import game.Callbacks
import game.GameState
import actors.messages._

private class AkkaSeatCallbacks(seatActor: ActorRef) extends Callbacks {
  def updatePlayers(playerA: Option[String], playerB: Option[String]) = seatActor ! UpdatePlayersOMsg(playerA, playerB)
  def updateBoard(playerId: PlayerId, board: Board) = seatActor ! UpdateBoardOMsg(playerId, board)
  def updateGameState(gameState: GameState) = seatActor ! UpdateStateOMsg(gameState)
}

class GameActor(params: GameParams) extends Actor with ActorLogging {
  var game: Game = new Game(params)
  val playerMap: BiMap[PlayerId, ActorRef] = HashBiMap.create() // TODO maybe not necessary?
  
  override def receive = LoggingReceive {
    case SubscriptionIMsg(playerName) => sitDown(playerName)
    case ChatMessageIMsg(msg) => chat(msg)
    case Terminated(seat) => standUp(seat)
    case InitBoardIMsg(board) => initBoard(board)
    case MakeMoveIMsg(move) => makeMove(move)
    case AskForParamsIMsg() => askForGameState()
    case other => log.error("unhandled: " + other)
  }
  
  private def sitDown(playerName: String) = {
    val playerId = game.sitDown(playerName, new AkkaSeatCallbacks(sender))
    playerId match {
      case None => sender ! SitDownFailOMsg()
      case Some(playerId) =>
        playerMap.put(playerId, sender)
        sender ! SitDownSuccessOMsg(playerId)
    }
  }
  
  private def chat(msg: String) = myPlayerId() match {
    case None => log.warning("Trying to chat while not sitting")
    case Some(playerId) => playerMap.values().foreach {
      _ ! ChatMessageOMsg(game.getPlayerName(playerId).get,msg)
    }
  }
  
  private def standUp(seat: ActorRef) = ???
  
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
    val playerId = playerMap.inverse().get(sender)
    if(playerId==null) None else Some(playerId)
  }
}