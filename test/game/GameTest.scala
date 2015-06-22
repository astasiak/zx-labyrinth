package actors

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import game.Callbacks
import game.Game
import game.GameParams
import game.Awaiting
import game.PlayerA
import game.PlayerB
import game.Board
import game.Ongoing
import game.GameState

class GameTest extends FunSuite with BeforeAndAfter with MockFactory {
  var game: Game = _
  
  val someBoard = Board.create((3,3), (0,0), (2,1), List())
  
  before {
    game = new Game(GameParams((3,3),5))
  }
  
  test("sitting down") {
    val callbacks1 = mock[Callbacks]
    val callbacks2 = mock[Callbacks]
    val callbacks3 = mock[Callbacks]
    (callbacks1.updatePlayers _).expects(Some("Tom"), None)
    val seat1 = game.sitDown("Tom", callbacks1)
    assert(seat1 === Some(PlayerA), "First sitting down should be successful")
    (callbacks1.updatePlayers _) expects(Some("Tom"), Some("Jerry"))
    (callbacks2.updatePlayers _) expects(Some("Tom"), Some("Jerry"))
    val seat2 = game.sitDown("Jerry", callbacks2)
    assert(seat2 === Some(PlayerB), "Second sitting down should be successful")
    (callbacks1.updatePlayers _) expects(*,*) never
    val seat3 = game.sitDown("Jim", callbacks3)
    assert(seat3 == None, "Third sitting down should fail")
    assert(game.gameState === Awaiting, "Game should be in the awaiting state")
  }
  
  test("starting game") {
    val callbacks = mock[Callbacks]
    (callbacks.updatePlayers _) expects(*,*) repeated 3 times
    val seat1 = game.sitDown("Tom", callbacks)
    val seat2 = game.sitDown("Jerry", callbacks)
    assert(game.gameState === Awaiting)
    
    (callbacks.updateBoard _) expects(*,*) repeated 4 times;
    game.initBoard(seat1.get, someBoard)
    assert(game.gameState == Awaiting)
    
    val initialStates = List(Ongoing(PlayerA), Ongoing(PlayerB))
    (callbacks.updateGameState _) expects(where[GameState]{initialStates.contains(_)}) repeated 2 times;
    game.initBoard(seat2.get, someBoard)
    assert(initialStates.contains(game.gameState))
  }
}