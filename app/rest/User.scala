package rest

import play.api.libs.json.Json
import java.time.LocalDateTime
import play.api.libs.json.Writes
import java.time.format.DateTimeFormatter
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

object User {
  case class UserRestModel(name: String, lastSeen: LocalDateTime, registered: LocalDateTime)
  
  implicit val usersWrites = Json.writes[UserRestModel]
  
  implicit def java8DateWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    val df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    def writes(d: LocalDateTime): JsValue = JsString(d.format(df))
  }
}