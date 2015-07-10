package actors

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite

import com.example.game.Awaiting;
import com.example.game.Board;
import com.example.game.PlayerA;
import com.example.game.PlayerB;

import game.Callbacks
import game.Game
import game.GameParams
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
    for(callback <- List(callbacks1,callbacks2,callbacks3)) {
      (callback.updateGameState _) expects(*) anyNumberOfTimes;
    }
    (callbacks1.updatePlayers _) expects(None,None)
    game.subscribe("Tom", callbacks1)
    (callbacks1.updatePlayers _).expects(Some("Tom"), None)
    val seat1 = game.sitDown("Tom")
    assert(seat1 === Some(PlayerA), "First sitting down should be successful")
    (callbacks2.updatePlayers _) expects(Some("Tom"), None)
    game.subscribe("Jerry", callbacks2)
    (callbacks1.updatePlayers _) expects(Some("Tom"), Some("Jerry"))
    (callbacks2.updatePlayers _) expects(Some("Tom"), Some("Jerry"))
    val seat2 = game.sitDown("Jerry")
    assert(seat2 === Some(PlayerB), "Second sitting down should be successful")
    (callbacks3.updatePlayers _) expects(Some("Tom"), Some("Jerry"))
    game.subscribe("Jim", callbacks3)
    (callbacks1.updatePlayers _) expects(*,*) never;
    val seat3 = game.sitDown("Jim")
    assert(seat3 == None, "Third sitting down should fail")
    assert(game.gameState === Awaiting, "Game should be in the awaiting state")
  }
  
  test("starting game") {
    val callbacks = mock[Callbacks]
    (callbacks.updateGameState _) expects(Awaiting) repeated 2 times;
    (callbacks.updatePlayers _) expects(*,*) repeated 5 times;
    game.subscribe("Tom", callbacks)
    val seat1 = game.sitDown("Tom")
    game.subscribe("Jerry", callbacks)
    val seat2 = game.sitDown("Jerry")
    assert(game.gameState === Awaiting)
    
    (callbacks.updateBoard _) expects(*,*) repeated 4 times;
    game.initBoard(seat1.get, someBoard)
    assert(game.gameState == Awaiting)
    
    val initialStates = List(Ongoing(PlayerA), Ongoing(PlayerB))
    (callbacks.updateGameState _) expects(*) repeated 2 times;
    game.initBoard(seat2.get, someBoard)
    assert(initialStates.contains(game.gameState))
  }
}