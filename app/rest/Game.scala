package rest

import play.api.libs.json.Json

object Game {
  
  case class GameRestModel(id: String, width: Int, height: Int, walls: Int, playerA: Option[String], playerB: Option[String], state: String)

  implicit val gamesWrites = Json.writes[GameRestModel]
}