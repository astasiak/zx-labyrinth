package actors

import akka.event.LoggingReceive
import akka.actor.Actor
import play.libs.Akka
import akka.actor.Props
import com.typesafe.scalalogging.LazyLogging
import scala.math
import game.Finished
import util.DateTimeUtil.ldtOrdering
import dao.MongoGameDao
import dao.MongoUserDao
import game.PlayerA
import game.PlayerB
import actors.messages.RankingUpdatedOMsg

class RatingActor extends Actor with LazyLogging {
  import RatingActor._
  val gameDao = MongoGameDao
  val userDao = MongoUserDao
  
  override def receive = LoggingReceive {
    case ProcessVictory(winner, loser) => processVictory(winner, loser)
    case RecalculateAllHistory => recalculateAllHistory()
  }
  
  private def calculateChange(xr: Int, yr: Int, won: Boolean) = {
    val d = yr - xr
    val we = 1.0/(1+math.pow(10,d/400.0))
    val wy = if(won) 1 else 0
    val diff = wy - we
    val k =
      if(xr<2100) 32
      else if(xr<2400) 24
      else 16
    math.round(k*diff).toInt
  }
  
  private def calculateChanges(winnerRating: Int, loserRating: Int) = {
    val winnerChange = calculateChange(winnerRating, loserRating, true)
    val loserChange = calculateChange(loserRating, winnerRating, false)
    (winnerChange, loserChange)
  }
  
  private def processVictory(winnerId: String, loserId: String) =
  {
    logger.debug(s"Processing rating for ${winnerId}/${loserId}")
    for(winner <- userDao.getUser(winnerId);
        loser <- userDao.getUser(loserId)) {
      val (winnerChange, loserChange) = calculateChanges(winner.rating, loser.rating)
      userDao.alterRating(winnerId, winnerChange)
      userDao.alterRating(loserId, loserChange)
      sender ! RankingUpdatedOMsg(List((winnerId,winnerChange), (loserId,loserChange)))
    }
  }
  
  private def recalculateAllHistory() = {
    logger.debug("Recalculating whole history of ratings")
    val games = gameDao.listGames()
        .filter(_.state.isInstanceOf[Finished])
        .sortBy(game=>(game.lastActive,game.id))
    val playerPairs = games.flatMap { game =>
      game.state match {
        case Finished(PlayerA) => Some((game.playerA.get.id, game.playerB.get.id))
        case Finished(PlayerB) => Some((game.playerB.get.id, game.playerA.get.id))
        case _ => None
      }
    }
    var ratingMap = Map[String,Int]() withDefaultValue(MongoUserDao.INIT_RATING)
    for((winnerId, loserId) <- playerPairs) {
      val winnerRating = ratingMap(winnerId)
      val loserRating = ratingMap(loserId)
      val (winnerChange, loserChange) = calculateChanges(winnerRating, loserRating)
      ratingMap += (winnerId->(winnerRating+winnerChange), loserId->(loserRating+loserChange))
    }
    val users = userDao.listUsers()
    for(user <- users) {
      userDao.setRating(user.name, ratingMap(user.name))
    }
    logger.info("Finished recalculating whole history of ratings")
  }
}

object RatingActor {
  case class ProcessVictory(winner: String, loser: String)
  case object RecalculateAllHistory
  
  val instance = Akka.system().actorOf(Props(classOf[RatingActor]))
}