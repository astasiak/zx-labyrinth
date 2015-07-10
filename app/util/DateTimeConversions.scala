package util

import java.time.LocalDateTime

/**
 * @author andrzej
 */
import java.util.Date
import java.time.ZoneId

object DateTimeConversions {
  class Java8DateWrapper(date: LocalDateTime) {
    def toDate() = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
  }
  implicit def wrapJava8Date(date: LocalDateTime) = new Java8DateWrapper(date)
  
  
  class OldJavaDateWrapper(date: Date) {
    def toLocalDateTime() = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
  implicit def wrapOldJavaDate(date: Date) = new OldJavaDateWrapper(date)
}