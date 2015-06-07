package actors

import game.Board

case class Move()
case class Position(x: Int, y: Int)
case object Directions extends Enumeration {
  val Up, Down, Left, Right = Value
}
case class MoveResult(newBoard: Board, keepGoing: Boolean)


case class PlayerId(id: String)

sealed trait InboudMessage
case class UnknownIMsg() extends InboudMessage
case class ChatMessageIMsg(msg: String) extends InboudMessage
case class SubscriptionIMsg() extends InboudMessage
case class ReadyForGameIMsg(board: Board) extends InboudMessage
case class MakeMoveIMsg(move: Move) extends InboudMessage
case class AskForGameStateIMsg() extends InboudMessage



sealed trait OutboundMessage
case class ChatMessageOMsg(msg: String, player: String) extends OutboundMessage
case class YourMoveOMsg() extends OutboundMessage
case class NotYourMoveOMsg() extends OutboundMessage
case class MoveResultOMsg(from: Position, dir: Directions.Value, success: Boolean, player: PlayerId) extends OutboundMessage
case class CurrentStateOMsg() extends OutboundMessage
case class GameEndedOMsg(winner: PlayerId) extends OutboundMessage
case class SitDownResultOMsg(success: Boolean) extends OutboundMessage

case class TechnicalMessageOMsg(msg: String) extends OutboundMessage