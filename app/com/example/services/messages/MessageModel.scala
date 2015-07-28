package com.example.services.messages

import com.example.game._

case class Position(x: Int, y: Int)
case class MoveResult(newBoard: Board, keepGoing: Boolean)

sealed trait InboudMessage
case class UnknownIMsg() extends InboudMessage
case class ErrorIMsg(msg: String) extends InboudMessage
case class ChatMessageIMsg(msg: String) extends InboudMessage
case class SubscriptionIMsg(playerName: String) extends InboudMessage
case class SitDownIMsg() extends InboudMessage
case class InitBoardIMsg(board: Board) extends InboudMessage
case class MakeMoveIMsg(move: Direction) extends InboudMessage
case class AskForParamsIMsg() extends InboudMessage
case class KeepAliveIMsg() extends InboudMessage
case class AskForGameInfoIMsg() extends InboudMessage



sealed trait OutboundMessage
case class ChatMessageOMsg(player: String, msg: String) extends OutboundMessage
case class PlayerPresenceOMsg(player: String, present: Boolean) extends OutboundMessage
case class SitDownSuccessOMsg(playerId: PlayerId) extends OutboundMessage
case class SitDownFailOMsg() extends OutboundMessage
case class InitBoardResultOMsg(success: Boolean) extends OutboundMessage
case class UpdateBoardOMsg(player: PlayerId, board: Board) extends OutboundMessage
case class UpdatePlayersOMsg(playerA: Option[String], playerB: Option[String]) extends OutboundMessage
case class UpdateStateOMsg(gameState: GameState) extends OutboundMessage
case class ParamsOMsg(params: GameParams) extends OutboundMessage
case class GameInfoOMsg(id: String, params: GameParams, playerA: Option[String], playerB: Option[String], gameState: GameState) extends OutboundMessage
case class RankingUpdatedOMsg(playerRatingChanges: List[(String,Int)]) extends OutboundMessage

case class TechnicalMessageOMsg(msg: String) extends OutboundMessage
case class ErrorOMsg(msg: String) extends OutboundMessage
