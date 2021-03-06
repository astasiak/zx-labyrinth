package com.example.game

import scala.collection.mutable.Map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.util.Random
import com.typesafe.scalalogging.LazyLogging

case class GameParams(size: Coord2D, walls: Int, afterFinish: Boolean, ranking: Boolean)

/** representation of states in which game can be: */
sealed trait GameState
case object Awaiting extends GameState
case class Ongoing(current: PlayerId) extends GameState
case class Finishing(winner: PlayerId) extends GameState
case class Finished(winner: PlayerId) extends GameState

/** model of player identifiers */
sealed trait PlayerId { def theOther: PlayerId }
case object PlayerA extends PlayerId { override def theOther = PlayerB }
case object PlayerB extends PlayerId { override def theOther = PlayerA }

/**
 * Game objects are mutable representation of the game in the whole lifecycle
 * from being initialized with given game parameters to joining game by players,
 * making their moves and verifying the result (winner) of the game.
 */
class Game(val params: GameParams) extends LazyLogging {
  
  private val players: Map[PlayerId, PlayerData] = Map()
  private val subscribers: Map[String, CallbackEntry] = Map()
  var gameState: GameState = Awaiting
  
  type PlayerInfo = Option[(String,Option[Board])]
  def putPlayers(playerA: PlayerInfo, playerB: PlayerInfo): Unit = {
    def putPlayer(playerId: PlayerId, playerInfo: PlayerInfo) =
      playerInfo map { case (userId, board)=>
        val playerData = new PlayerData(userId)
        playerData.board = board
        players.put(playerId, playerData)
      }
    putPlayer(PlayerA, playerA)
    putPlayer(PlayerB, playerB)
  }
  
  def subscribe(userId: String, callbacks: Callbacks, admin: Boolean = false, initCallbacks: Boolean = true) = {
    if(subscribers.contains(userId)) {
      logger.info(s"User ${userId} failed to subscribe to the game")
      false
    }
    else {
      logger.debug(s"User ${userId} subscribed to the game")
      subscribers += userId->CallbackEntry(callbacks,admin)
      if(initCallbacks) {
        callbacks.updatePlayers(
            players.get(PlayerA).map(_.userId),
            players.get(PlayerB).map(_.userId))
        callbacks.updateGameState(gameState)
        val gameFinished = gameState match {case Finished(_)=> true; case _=>false}
        for((id, playerData) <- players) {
          val board = playerData.board
          val boardToSend =
            if(gameFinished || admin || userId==playerData.userId) board
            else board.map(_.privatize)
          if(boardToSend!=None) callbacks.updateBoard(id, boardToSend.get)
        }
      }
      true
    }
  }
  
  def unsubscribe(userId: String): Unit = {
    logger.debug(s"User ${userId} unsubscribed from the game")
    subscribers -= userId
  }
  
  def sitDown(userId: String): Option[PlayerId] = {
    val (playerId,_) = getPlayerData(userId)
    if(playerId!=None) {
      logger.info(s"User ${userId} cannot sit down twice")
      return None
    }
    if(!subscribers.contains(userId)) {
      logger.info(s"User ${userId} cannot sit down if not subscribed")
      return None
    }
    for(playerId <- (Set[PlayerId](PlayerA, PlayerB)--players.keys)) {
      logger.debug(s"User ${userId} sat down as a ${playerId}")
      players.put(playerId, new PlayerData(userId))
      subscribers.values.foreach {
        _.callback.updatePlayers(
            players.get(PlayerA).map(_.userId),
            players.get(PlayerB).map(_.userId))
      }
      for {
        player <- players.get(playerId.theOther)
        board <- player.board
        subscriber <- subscribers.get(userId)
      } subscriber.callback.updateBoard(playerId.theOther, board.privatize)
      return Some(playerId)
    }
    return None
  }
  
  def initBoard(playerId: PlayerId, board: Board): Boolean =
    (gameState, players.get(playerId)) match {
    case (Awaiting, Some(player)) => if(player.board!=None) {
      logger.debug(s"Player ${playerId} (${player.userId}) tries to reinitialize their board")
      return false
    } else if(!isBoardAcceptable(board)) {
      logger.info(s"Player ${playerId} (${player.userId}) declared unacceptable board\n${board.toFancyString}")
      return false
    } else {
      logger.debug(s"Player ${playerId} (${player.userId}) initiated their board\n${board.toFancyString}")
      player.board = Some(board)
      if(players.get(playerId.theOther).flatMap(_.board)!=None) {
        val startingPlayer = Random.shuffle(List(PlayerA,PlayerB)).head
        gameState = Ongoing(startingPlayer)
        subscribers.values.foreach(_.callback.updateGameState(gameState))
      }
      sendBoardToPlayers(playerId)
      return true
    }
    case (_, None) =>
      logger.info(s"Player ${playerId} cannot init board for empty seat")
      return false
    case _ =>
      logger.info(s"Player ${playerId} cannot init board when game has started")
      return false
  }
  
  def declareMove(playerId: PlayerId, direction: Direction) = gameState match {
    case Ongoing(currentPlayer) if playerId==currentPlayer =>
      processMove(playerId, direction, false)
    case Finishing(winner) if playerId==winner.theOther =>
      processMove(playerId, direction, true)
    case _ =>
      logger.info("Player cannot move now")
  }
  
  // FIXME too imperative
  private def processMove(playerId: PlayerId, direction: Direction, afterGame: Boolean) = {
    val player = players.get(playerId.theOther).get
    val result = player.board.get.makeMove(direction)
    logger.debug(s"Player ${playerId} declares move in direction ${direction} with success:${result.success}")
    player.board = Some(result.newBoard)
    gameState =
      if(afterGame) Finishing(playerId.theOther)
      else if(result.success) Ongoing(playerId)
      else Ongoing(playerId.theOther)
    if(result.newBoard.isFinished) {
      if(afterGame) {
        gameState = Finished(playerId.theOther)
      } else {
        val winnerId = players.get(playerId).get.userId
        val loserId = players.get(playerId.theOther).get.userId
        subscribers.values.foreach(_.callback.onFinish(winnerId, loserId))
        logger.debug(s"Game has finished and the winner is ${players.get(playerId).get.userId}")
        gameState = if(params.afterFinish) Finishing(playerId) else Finished(playerId)
      }
    }
    if(gameState.isInstanceOf[Finished]) {
      sendUncoveredBoards()
    } else {
      sendBoardToPlayers(playerId.theOther)
    }
    subscribers.values.foreach(_.callback.updateGameState(gameState))
  }
  
  def getPlayerData(userId: String): (Option[PlayerId], Option[Board]) = {
    val playerEntry = players.find { _._2.userId == userId }
    (playerEntry.map(_._1), playerEntry.flatMap(_._2.board))
  }
  
  def getPlayerNames() = {
    def getPlayerName(playerId: PlayerId) = players.get(playerId).map(_.userId)
    (getPlayerName(PlayerA),getPlayerName(PlayerB))
  }
  
  private def sendBoardToPlayers(playerId: PlayerId) = {
    val player = players(playerId)
    val board = player.board.get
    subscribers.foreach { case (userId,CallbackEntry(callback,admin))=>
      val boardToSend = if(userId==player.userId || admin) board else board.privatize
      callback.updateBoard(playerId, boardToSend)
    }
  }
  
  private def sendUncoveredBoards() = players.foreach(boardOwner=>{
    subscribers.values.foreach(receiver=>{
      receiver.callback.updateBoard(boardOwner._1, boardOwner._2.board.get)
    })
  })
  
  private def isBoardAcceptable(board: Board) = {
    board.isValid &&
    board.numberOfBorders<=params.walls &&
    board.size==params.size
  }
}
// internal representation of player data
private class PlayerData(val userId: String) {
  var board: Option[Board] = None
}

/** trait for implementing callback handler on the client side */
trait Callbacks {
  def updatePlayers(playerA: Option[String], playerB: Option[String]): Unit
  def updateBoard(player: PlayerId, board: Board): Unit
  def updateGameState(gameState: GameState): Unit
  def onFinish(winner: String, loser: String): Unit
}

private case class CallbackEntry(callback: Callbacks, admin: Boolean)
