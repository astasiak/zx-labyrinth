package actors

import org.junit.Test
import org.junit.Assert._
import org.junit.Before
import game._

class BoardTest {
  
  var board: Board = _
 
  @Before def init {
    board = Board((3,3),(0,0),(2,1),List(
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
    assertEquals(false, board.isFinished)
    val moves = List(South, East, South)
    for(move <- moves) {
      val result = board.makeMove(move)
      assertEquals("Unsuccessful move", true, result.success)
      board = result.newBoard
    }
    assertEquals("Not finished when expected", true, board.isFinished)
  }
  
  @Test def wrongPathsTest {
    val moves = List(
        (East, false, false),
        (South, true, false),
        (South, false, false),
        (East, true, false),
        (South, true, true))
    for((dir, expectedSuccess, expectedFinished) <- moves) {
      val result = board.makeMove(dir)
      val (x, y) = board.position
      val borderPassed = ()=>(if(List(South,North).contains(dir)) board.borders.horizontal else board.borders.vertical)(x)(y)
      assertEquals("Prematurely discovered border", false, borderPassed().discovered)
      board = result.newBoard
      assertEquals("Did not discover passed border", true, borderPassed().discovered)
      assertEquals("Wrong move success calculation", expectedSuccess, result.success)
      assertEquals("Wrong finish calculation", expectedFinished, board.isFinished)
    }
  }
  
  @Test def illegalPathsTest {
    val moves = List(
        (North, false), (West, false), (South, true), (East, true),
        (North, true), (North, false), (East, true), (East, false),
        (South, true), (South, true), (South, false))
    for((dir, expectedSuccess) <- moves) {
      val result = board.makeMove(dir)
      board = result.newBoard
      assertEquals("Wrong move success calculation", expectedSuccess, result.success)
    }
  }
 
}