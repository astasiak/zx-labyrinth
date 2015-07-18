package rest

import play.api.libs.json.Json
import java.time.LocalDateTime

object Game {
  
  case class GameRestModel(
      id: String,
      width: Int,
      height: Int,
      walls: Int,
      playerA: Option[String],
      playerB: Option[String],
      state: String,
      lastActive: LocalDateTime,
      created: LocalDateTime,
      inMemory: Boolean)

  import util.DateTimeUtil.java8DateWrites
  implicit val gamesWrites = Json.writes[GameRestModel]
}