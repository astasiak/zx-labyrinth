package com.example.services

import akka.event.LoggingReceive
import akka.actor.Terminated
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import com.google.common.collect.HashBiMap
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import com.typesafe.scalalogging.LazyLogging
import com.example.game._
import com.example.services.messages._
import com.example.dao.MongoGameDao
import com.example.dao.GameModel
import com.example.dao.GameDao
import java.time.LocalDateTime
import com.example.util.DateTimeUtil
import com.example.dao.DataAccessLayer

trait GameActorComponent {
  this: DataAccessLayer with RatingActorComponent =>
  
  class GameActor(id: String, game: Game) extends Actor with ActorLogging {
    val playerMap: Map[ActorRef, (Option[PlayerId],String)] = Map()
    
    game.subscribe("_MONGO", new DaoCallbacks(gameDao), admin=true, initCallbacks=false)
    
    def this(id: String, params: GameParams) {
      this(id,new Game(params))
      val currentTime = DateTimeUtil.now
      gameDao.saveGame(GameModel(id, game.params, game.gameState, None, None, currentTime, currentTime))
    }
    
    override def receive = LoggingReceive {
      case SubscriptionIMsg(playerName) => subscribe(playerName)
      case SitDownIMsg() => sitDown()
      case ChatMessageIMsg(msg) => chat(msg)
      case Terminated(seat) => unsubscribe()
      case InitBoardIMsg(board) => initBoard(board)
      case MakeMoveIMsg(move) => makeMove(move)
      case AskForParamsIMsg() => askForGameState()
      case AskForGameInfoIMsg() => askForGameInfo()
      case KeepAliveIMsg() => log.debug("Connection kept alive")
      case RankingUpdatedOMsg(playerRatingChanges) => updateRating(playerRatingChanges) // from RatingActor
      case other => log.error("unhandled: " + other)
    }
    
    private def subscribe(userId: String) = {
      val success = !userId.startsWith("_") && game.subscribe(userId, new AkkaSeatCallbacks(sender))
      if(success) {
        context watch sender
        val (playerId, board) = game.getPlayerData(userId)
        playerMap += sender->(playerId,userId)
        playerMap.keys foreach {
          _ ! PlayerPresenceOMsg(userId,true)
        }
        playerId.foreach(sender ! SitDownSuccessOMsg(_))
        board.foreach(_=> sender ! InitBoardResultOMsg(true))
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
          playerMap += sender -> (Some(playerId),userId)
          sender ! SitDownSuccessOMsg(playerId)
      }
    }
    
    private def chat(msg: String) = playerMap.get(sender).map(_._2) match {
      case None => log.warning("Trying to chat while not being subscribed")
      case Some(userId) => playerMap.keys.foreach { receiver=>
        receiver ! ChatMessageOMsg(userId,msg)
      }
    }
    
    private def initBoard(board: Board) = myPlayerId match {
      case None => log.warning("Trying to init board while not sitting")
      case Some(playerId) =>
        val success = game.initBoard(playerId, board)
        sender ! InitBoardResultOMsg(success)
    }
    
    private def makeMove(move: Direction) = myPlayerId match {
      case None => log.warning("Trying to make move while not sitting")
      case Some(playerId) => game.declareMove(playerId, move)
    }
    
    private def askForGameState() = sender ! ParamsOMsg(game.params)
    
    private def askForGameInfo() = {
      val (nameA, nameB) = game.getPlayerNames()
      sender ! GameInfoOMsg(id, game.params, nameA, nameB, game.gameState)
    }
    
    private def updateRating(playerRatingChanges: List[(String,Int)]) = playerMap.keys.foreach { receiver=>
      receiver ! RankingUpdatedOMsg(playerRatingChanges)
    }
    
    private def myPlayerId: Option[PlayerId] = playerMap.get(sender).flatMap(_._1)
  
    private class DaoCallbacks(gameDao: GameDao) extends Callbacks {
      def updatePlayers(playerA: Option[String], playerB: Option[String]) = gameDao.updatePlayers(id, playerA, playerB)
      def updateBoard(playerId: PlayerId, board: Board) = gameDao.updateBoard(id, playerId, board)
      def updateGameState(gameState: GameState) = gameDao.updateGameState(id, gameState)
      def onFinish(winner: String, loser: String) = RatingActor.instance ! RatingActor.ProcessVictory(winner, loser)
    }
  }
  
  private class AkkaSeatCallbacks(seatActor: ActorRef) extends Callbacks {
    def updatePlayers(playerA: Option[String], playerB: Option[String]) = seatActor ! UpdatePlayersOMsg(playerA, playerB)
    def updateBoard(playerId: PlayerId, board: Board) = seatActor ! UpdateBoardOMsg(playerId, board)
    def updateGameState(gameState: GameState) = seatActor ! UpdateStateOMsg(gameState)
    def onFinish(winner: String, loser: String) = {} // TODO maybe send something here instead of updateGame state (popups)
  }
}