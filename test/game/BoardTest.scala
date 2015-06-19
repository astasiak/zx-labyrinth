package actors

import game._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

class BoardTest extends FunSuite with BeforeAndAfter {
  
  var board: Board = _
 
  before {
    board = TestUtils.mkBoard(
       "S|   \n"+
       " + + \n"+
       "   | \n"+
       "-+ + \n"+
       "  m  "
    )
  }
  
  test("modelling successful paths") {
    assert(false === board.isFinished)
    val moves = List(South, East, South)
    for(move <- moves) {
      val result = board.makeMove(move)
      assert(true === result.success, "Unsuccessful move")
      board = result.newBoard
    }
    assert(true === board.isFinished, "Not finished when expected")
  }
  
  test("modelling wrong paths") {
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
      assert(false === borderPassed().discovered, "Prematurely discovered border")
      board = result.newBoard
      assert(true === borderPassed().discovered, "Did not discover passed border")
      assert(expectedSuccess === result.success, "Wrong move success calculation")
      assert(expectedFinished === board.isFinished, "Wrong finish calculation")
    }
  }
  
  test("modelling illegal paths")  {
    val moves = List(
        (North, false), (West, false), (South, true), (East, true),
        (North, true), (North, false), (East, true), (East, false),
        (South, true), (South, true), (South, false))
    for((dir, expectedSuccess) <- moves) {
      val result = board.makeMove(dir)
      board = result.newBoard
      assert(expectedSuccess === result.success)
    }
  }
  
  test("privatizing board") {
    val board1 = TestUtils.mkBoard(
       "S    \n"+
       " + + \n"+
       "     \n"+
       " + + \n"+
       "  m  ")
    val board2 = TestUtils.mkBoard(
       "SI   \n"+
       " + + \n"+
       "     \n"+
       " + + \n"+
       "  m  ")
    val board3 = TestUtils.mkBoard(
       "sI   \n"+
       ".+ + \n"+
       "!    \n"+
       "=+ + \n"+
       "  m  ")
    assert(board.privatize === board1)
    board = board.makeMove(East).newBoard
    assert(board.privatize === board2)
    board = board.makeMove(South).newBoard.makeMove(South).newBoard
    assert(board.privatize === board3)
  }
  
  test("validating board") {
    val correctBoard = TestUtils.mkBoard(
        "S|   \n"+
        " +-+ \n"+
        " |   \n"+
        " + + \n"+
        "   |m")
    val invalidBoard = TestUtils.mkBoard(
        "S|   \n"+
        " +-+ \n"+
        " | | \n"+
        " + + \n"+
        "   |m")
    assert(correctBoard.isValid === true)
    assert(invalidBoard.isValid === false)
  }
 
}