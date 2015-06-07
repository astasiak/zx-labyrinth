package actors

import org.junit.Test
import org.junit.Assert._
import org.junit.Before
import game.Board
import game.North
import game.East
import game.South
import game.ProtoBorder

class BoardTest {
  
  var initBoard: Board = _
 
  @Before def init {
    initBoard = Board((3,3),(0,0),(2,1),List(
        ProtoBorder(0,0,true),
        ProtoBorder(1,1,true),
        ProtoBorder(1,0,false)))
      // +-+-+-+
      // |S|   |
      // + + + +
      // |   | |
      // +-+ + +
      // |  M  |
      // +-+-+-+
  }
  
  @Test def successfulPathTest {
    assertEquals(false, initBoard.isFinished)
    val moves = List(South, East, South)
    for(move <- moves) {
      val result = initBoard.makeMove(move)
      assertEquals("Unsuccessful move", true, result.success)
      initBoard = result.newBoard
    }
    assertEquals("Not finished when expected", true, initBoard.isFinished)
  }
  
  @Test def wrongPathsTest {
    assertEquals("Prematurely finished", false, initBoard.isFinished)
    assertEquals("Wrong initial state", false, initBoard.borders.vertical(0)(0).discovered)
    var result = initBoard.makeMove(East)
    initBoard = result.newBoard
    assertEquals("Successful move", false, result.success)
    assertEquals("Discovered border on move", true, initBoard.borders.vertical(0)(0).discovered)
    assertEquals("Prematurely finished", false, initBoard.isFinished)
    assertEquals("Wrong initial state",false, initBoard.borders.horizontal(0)(0).discovered)
    result = initBoard.makeMove(South)
    initBoard = result.newBoard
    assertEquals("Unsuccessful move", true, result.success)
    assertEquals("Discovered border on move", true, initBoard.borders.horizontal(0)(0).discovered)
    assertEquals("Prematurely finished", false, initBoard.isFinished)
    result = initBoard.makeMove(South)
    initBoard = result.newBoard
    assertEquals("Successful move", false, result.success)
    assertEquals("Prematurely finished", false, initBoard.isFinished)
    result = initBoard.makeMove(East)
    initBoard = result.newBoard
    assertEquals("Prematurely finished", false, initBoard.isFinished)
    result = initBoard.makeMove(South)
    initBoard = result.newBoard
    assertEquals("Not finished when expected", true, initBoard.isFinished)
  }
 
}