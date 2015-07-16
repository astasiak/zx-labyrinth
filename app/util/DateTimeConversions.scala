package util

import java.time.LocalDateTime
import java.util.Date
import java.time.ZoneId
import play.api.libs.json.Writes
import java.time.format.DateTimeFormatter
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

object DateTimeConversions {
  class Java8DateWrapper(date: LocalDateTime) {
    def toDate() = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
  }
  implicit def wrapJava8Date(date: LocalDateTime) = new Java8DateWrapper(date)
  
  
  class OldJavaDateWrapper(date: Date) {
    def toLocalDateTime() = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
  implicit def wrapOldJavaDate(date: Date) = new OldJavaDateWrapper(date)
  
  
  implicit def java8DateWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    val df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    def writes(d: LocalDateTime): JsValue = JsString(d.format(df))
  }
}