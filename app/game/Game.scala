package game

import scala.collection.mutable.Map

case class GameParams(size: (Int, Int), walls: Int)

// representation of states in which game can be:
sealed trait GameState
case object Awaiting extends GameState
case class Ongoing(current: PlayerId) extends GameState
case class Finished(winner: PlayerId) extends GameState

// model of player identifiers
sealed trait PlayerId { def theOther: PlayerId }
case object PlayerA extends PlayerId { override def theOther = PlayerB }
case object PlayerB extends PlayerId { override def theOther = PlayerA }

// main implementation of game internal logic
class Game(val params: GameParams) {
  private val players: Map[PlayerId, PlayerData] = Map()
  var gameState: GameState = Awaiting
  
  def privatize(board: Board): Board = board // TODO
  
  def sitDown(playerName: String, callbacks: Callbacks): Option[PlayerId] = {
    for(playerId <- List(PlayerA, PlayerB).filter(!players.contains(_))) {
      players.put(playerId, new PlayerData(callbacks, playerName))
      players.values.foreach {
        _.callbacks.updatePlayers(
            players.get(PlayerA).map(_.name),
            players.get(PlayerB).map(_.name))
      }
      return Some(playerId)
    }
    None
  }
  
  def initBoard(playerId: PlayerId, board: Board) = (gameState, players.get(playerId)) match {
    case (Awaiting, Some(player)) => {
      // TODO: validation of size and number of walls
      player.board = Some(board)
      if(players.get(playerId.theOther).flatMap(_.board)!=None) {
        gameState = Ongoing(PlayerA)
        players.values.foreach(_.callbacks.updateGameState(gameState))
      }
      players.values.foreach(_.callbacks.updateBoard(playerId, privatize(board)))
    }
    case (_, None) =>
      throw new RuntimeException("Cannot init board for empty seat")
    case _ =>
      throw new RuntimeException("Cannot init board when game has started")
  }
  
  def declareMove(playerId: PlayerId, direction: Direction) = gameState match {
    case Ongoing(currentPlayer) if playerId==currentPlayer => {
      val player = players.get(playerId).get
      val result = player.board.get.makeMove(direction)
      player.board = Some(result.newBoard)
      players.values.foreach(_.callbacks.updateBoard(playerId, result.newBoard))
      gameState =
        if(result.newBoard.isFinished) Finished(playerId)
        else if(result.success) Ongoing(playerId)
        else Ongoing(playerId.theOther)
      players.values.foreach(_.callbacks.updateGameState(gameState))
    }
    case Ongoing(currentPlayer) if playerId!=currentPlayer =>
      throw new RuntimeException("Cannot move during the opponent's turn")
    case _ =>
      throw new RuntimeException("Game has to be ongoing to make move")
  }
}
// internal representation of player data
private class PlayerData(val callbacks: Callbacks, val name: String) {
  var board: Option[Board] = None
}

// trait for implementing callback handler on the client side
trait Callbacks {
  def updatePlayers(playerA: Option[String], playerB: Option[String])
  def updateBoard(player: PlayerId, board: Board)
  def updateGameState(gameState: GameState)
}