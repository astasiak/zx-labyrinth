package rest

import play.api.libs.json.Json
import java.time.LocalDateTime
import play.api.libs.json.Writes
import java.time.format.DateTimeFormatter
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

object User {
  case class UserRestModel(name: String, lastSeen: LocalDateTime, registered: LocalDateTime)
  
  import util.DateTimeConversions.java8DateWrites
  implicit val usersWrites = Json.writes[UserRestModel]
}