package actors

import akka.actor.Actor
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.ActorLogging

class GameActor(params: GameParameters) extends Actor with ActorLogging {
  val playerA: PlayerData = new PlayerData("Player A")
  val playerB: PlayerData = new PlayerData("Player B")
  val players = Set(playerA, playerB)
  var status: GameStatus.Value = GameStatus.New
  // gameFinished / winner / ongoing
  
  override def receive = LoggingReceive {
    case ChatMessageIMsg(msg) => chat(msg)
    case SubscriptionIMsg => sitDown()
    case Terminated(seat) => standUp(seat)
    case ReadyForGameIMsg(boardConfiguration) => readyForGame(boardConfiguration)
    case MakeMoveIMsg(move) => makeMove(move)
    case AskForGameStateIMsg() => askForGameState()
    case other => log.error("unhandled: " + other)
  }
  
  private def chat(msg: String) = myPlayerData() match {
    case None => log.warning("Trying to start while not sitting")
    case Some(playerData) => players.flatMap(_.seat).foreach(_ ! ChatMessageOMsg(playerData.name,msg))
  }
  
  private def sitDown() = (playerA.seat, playerB.seat) match {
    case (None,_) => sitDownThere(playerA)
    case (Some(_), None) => sitDownThere(playerB)
    case (Some(_), Some(_)) => sender ! SitDownResultOMsg(false)
  }
  
  private def sitDownThere(playerData: PlayerData) {
    playerData.seat = Some(sender);
    context watch sender
    // send OK
    // save name
  }
  
  private def myPlayerData() = {
    if(Some(sender)==playerA.seat) Some(playerA)
    else if(Some(sender)==playerB.seat) Some(playerB)
    else None
  }
  
  private def theOtherPlayerData() = {
    if(Some(sender)==playerA.seat) Some(playerB)
    else if(Some(sender)==playerA.seat) Some(playerA)
    else None
  }
  
  private def standUp(seat: ActorRef) = myPlayerData() match {
    case None => log.warning("Trying to stand up while not sitting")
    case Some(playerData) => {
      playerData.seat = None
      playerData.ready = false
    }
  }
  
  private def readyForGame(boardConfiguration: BoardConfiguration) = myPlayerData() match {
    case None => log.warning("Trying to start while not sitting")
    case Some(playerData) => {
      playerData.board = boardConfiguration
      playerData.ready = true
      if(theOtherPlayerData().map(_.ready).getOrElse(false)) {
        startGame()
      }
    }
  }
  
  private def startGame() = {
    if(status!=GameStatus.New) {
      log.warning("Trying to start already started game")
    } else if(playerA.seat==None || playerB.seat==None) {
      log.warning("Trying to start a game without players")
    } else {
      status = GameStatus.Ongoing
      playerA.playing = true
      playerB.playing = false
      playerA.seat.map(_ ! YourMoveOMsg)
    }
  }
  
  private def makeMove(move: Move) = {
    if(!myPlayerData().get.playing) {
      sender ! NotYourMoveOMsg
    } else {
      
    }
  }
  
  private def askForGameState() = {
    sender ! TechnicalMessageOMsg("received ASK")
  }
}

class PlayerData(val name: String) {
  var seat: Option[ActorRef] = None
  var board: BoardConfiguration = BoardConfiguration()
  var ready: Boolean = false
  var playing: Boolean = false
}

case object GameStatus extends Enumeration {
  val New, Ongoing, Finished = Value
}