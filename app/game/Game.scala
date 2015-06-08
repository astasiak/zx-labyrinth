package game

case class GameParams(size: (Int, Int), walls: Int)
case object GameState extends Enumeration {
  val Awaiting, Ongoing, Finished = Value
}

class Game(val params: GameParams) {
  var playerA: Option[PlayerData] = None
  var playerB: Option[PlayerData] = None
  var currentPlayerA: Boolean = true
  var winnerPlayerA: Option[Boolean] = None
  var gameState: GameState.Value = GameState.Awaiting
  
  def privatizeBoard(board: Board): Board = board // TODO
  
  def sendStates() = {
    playerA.map {
      val gs: GameStatus = GameStatus(currentPlayerA,
          (playerA.map(_.board).flatten, playerB.map(_.board).flatten.map(privatizeBoard _)),
          gameState, winnerPlayerA)
      _.callbacks.updateStatus(gs)
    }
    playerB.map {
      val gs: GameStatus = GameStatus(!currentPlayerA,
          (playerA.map(_.board).flatten.map(privatizeBoard _), playerB.map(_.board).flatten),
          gameState, winnerPlayerA.map(!_))
      _.callbacks.updateStatus(gs)
    }
  }
  
  def sitDown(playerName: String, callbacks: Callbacks): Option[String] = (playerA, playerB) match {
    case (None,_) => { playerA = Some(new PlayerData(callbacks, playerName)); Some("A") }
    case (Some(_), None) => { playerB = Some(new PlayerData(callbacks, playerName)); Some("B") }
    case (Some(_), Some(_)) => None
  }
  
  def initBoard(playerId: String, board: Board) = (playerId match {
    case "A" => playerA
    case "B" => playerB
    case _ => None
  }).map(p=>{
    p.board = Some(board);
    if(playerA.flatMap(_.board).isDefined && playerB.flatMap(_.board).isDefined) {
      gameState = GameState.Ongoing;
      sendStates();
    }
  })
}


class PlayerData(val callbacks: Callbacks, val name: String) {
  var board: Option[Board] = None
}

case class GameStatus(yourMove: Boolean, boards: (Option[Board], Option[Board]), gameStatus: GameState.Value, youWon: Option[Boolean])

trait Callbacks {
  def updateStatus(status: GameStatus)
}