package dao

import java.time.LocalDateTime
import game.Board
import game.GameState
import game.GameParams

case class UserModel(
    name: String,
    password: String,
    lastSeen: LocalDateTime,
    registered: LocalDateTime,
    rating: Int)

case class GamePlayerModel(
    id: String,
    board: Option[Board])

case class GameModel(
    id: String,
    params: GameParams,
    state: GameState,
    playerA: Option[GamePlayerModel],
    playerB: Option[GamePlayerModel],
    lastActive: LocalDateTime,
    created: LocalDateTime)