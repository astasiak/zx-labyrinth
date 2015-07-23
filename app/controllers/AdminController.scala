package controllers

import com.typesafe.scalalogging.LazyLogging

import actors.RatingActor
import akka.actor.actorRef2Scala
import dao.MongoUserDao
import dao.UserDao
import play.Play
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Results
import util.PasswordHasher

object AdminController extends Controller with LazyLogging {

  val userDao: UserDao = MongoUserDao

  val secretAdminKeyHash = Play.application().configuration().getString("secretAdminKeyHash")
  
  def dropUsers = securedAction {
    userDao.dropAllUsers
    Ok("OK")
  }
  
  def recalculateRatings = securedAction { 
    RatingActor.instance ! RatingActor.RecalculateAllHistory
    Ok("OK")
  }
  
  private def securedAction(action: => Result) = Action { request =>
    request.headers.get("secret") match {
      case Some(requestSecret) if PasswordHasher.check(requestSecret, secretAdminKeyHash) => action
      case _ => Results.Forbidden("You are not allowed to do this!")
    }
  }
}