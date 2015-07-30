package com.example.services

import akka.event.LoggingReceive
import akka.actor.Actor
import play.libs.Akka
import akka.actor.Props
import com.typesafe.scalalogging.LazyLogging
import scala.math
import com.example.util.DateTimeUtil.ldtOrdering
import com.example.dao.MongoGameDao
import com.example.dao.MongoUserDao
import com.example.game.PlayerA
import com.example.game.PlayerB
import com.example.game.Finished
import com.example.services.messages.RankingUpdatedOMsg
import com.example.dao.DataAccessLayer
import com.example.dao.UserDao

trait RatingActorComponent {
  this: DataAccessLayer =>

  class RatingActor extends Actor with LazyLogging {
    import RatingActor._
    
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
      val games = gameDao.listGames(None, None)
          .filter(_.state.isInstanceOf[Finished])
          .filter(_.params.ranking) // TODO: db query
          .sortBy(game=>(game.lastActive,game.id))
      val playerPairs = games.flatMap { game =>
        game.state match {
          case Finished(PlayerA) => Some((game.playerA.get.id, game.playerB.get.id))
          case Finished(PlayerB) => Some((game.playerB.get.id, game.playerA.get.id))
          case _ => None
        }
      }
      var ratingMap = Map[String,Int]() withDefaultValue(userDao.INIT_RATING)
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
    
    val instance = Akka.system().actorOf(Props(classOf[RatingActor], RatingActorComponent.this))
  }
}