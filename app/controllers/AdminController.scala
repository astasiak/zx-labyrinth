package controllers

import scala.Left
import scala.Right
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import actors.RoomManager
import actors.SeatActor
import dao.MongoUserDao
import dao.UserDao
import com.typesafe.scalalogging.LazyLogging
import controllers.FormMappings._
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results
import play.api.mvc.WebSocket
import play.Play

object AdminController extends Controller with LazyLogging {

  val userDao: UserDao = MongoUserDao

  val secretAdminKey = Play.application().configuration().getString("secretAdminKey")
  
  def dropUsers() = Action { request =>
    request.headers.get("secret") match {
      case Some(requestSecret) if requestSecret==secretAdminKey => {
        userDao.dropAllUsers
        Ok("OK")
      }
      case _ => Results.Forbidden("You are not allowed to do this!")
    }
  }
}