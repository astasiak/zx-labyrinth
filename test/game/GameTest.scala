package actors

import org.junit.Test
import org.junit.Assert._
import org.junit.Before
import game._
import org.mockito.Mockito
import org.scalatest.FunSuite
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter

class GameTest extends FunSuite with BeforeAndAfter with MockFactory {
  var game: Game = _
  
  before {
    game = new Game(GameParams((3,3),5))
  }
  
  test("some test") {
    val callbacks = mock[Callbacks]
    //(callbacks.updateBoard _).expects(PlayerA, *).returning(value)
  }
}