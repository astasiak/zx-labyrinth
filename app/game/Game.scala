package game

import scala.collection.mutable.Map

case class GameParams(size: (Int, Int), walls: Int)

// representation of states in which game can be:
sealed trait GameState
case class Awaiting() extends GameState
case class Ongoing(current: PlayerId) extends GameState
case class Finished(winner: PlayerId) extends GameState

// model of player identifiers
sealed trait PlayerId { def theOther: PlayerId }
case object PlayerA extends PlayerId { override def theOther = PlayerB }
case object PlayerB extends PlayerId { override def theOther = PlayerA }

// main implementation of game internal logic
class Game(val params: GameParams) {
  private val players: Map[PlayerId, PlayerData] = Map()
  var gameState: GameState = Awaiting()
  
  def privatize(board: Board): Board = board // TODO
  
  def sitDown(playerName: String, callbacks: Callbacks): Option[PlayerId] = {
    for(playerId <- List(PlayerA, PlayerB).filter(!players.contains(_))) {
      players.put(playerId, new PlayerData(callbacks, playerName))
      players.foreach {
        _._2.callbacks.updatePlayers(players(PlayerA).name, players(PlayerA).name)
      }
      return Some(playerId)
    }
    None
  }
  
  def initBoard(playerId: PlayerId, board: Board) = players.get(playerId) match {
    case None => throw new RuntimeException("Cannot init board for empty seat")
    case Some(player) => {
      player.board = Some(board)
      players.get(playerId.theOther).map(_.callbacks.updateBoard(playerId, privatize(board)))
    }
  }
  
  def declareMove(playerId: PlayerId, direction: Direction) = {
    
  }
}
// internal representation of player data
private class PlayerData(val callbacks: Callbacks, val name: String) {
  var board: Option[Board] = None
}

// trait for implementing callback handler on the client side
trait Callbacks {
  def updatePlayers(playerA: String, playerB: String)
  def updateBoard(player: PlayerId, board: Board)
  def updateGameState(gameState: GameState)
}