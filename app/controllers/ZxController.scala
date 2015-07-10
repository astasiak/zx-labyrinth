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

object ZxController extends Controller with LazyLogging {

  val userDao: UserDao = MongoUserDao

  def index = Action { request =>
    val userName = request.session.get("user")
    Ok(views.html.index(userName))
  }

  def createGame = Action { request =>
    val gameId = RoomManager.createGame(request)
    Redirect(routes.ZxController.game(gameId))
  }

  def game(gameId: String) = Action { implicit request =>
    request.session.get("user").map { user =>
      Ok(views.html.game(gameId))
    }.getOrElse {
      Results.NotFound(views.html.error("You need to log in"))
    }
  }

  def listGames() = Action {
    Ok(views.html.list())
  }

  def gameWs(gameId: String) = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    Future.successful((RoomManager.room(gameId), request.session.get("user")) match {
      case (Some(actorRef), Some(user)) => Right(SeatActor.props(actorRef, user))
      case _                            => Left(Forbidden)
    })
  }

  def again(gameId: String) = Action {
    RoomManager.getContinuation(gameId).map { newGameId =>
      Redirect(routes.ZxController.game(newGameId))
    }.getOrElse {
      Results.NotFound(views.html.error("Game " + gameId + " not found"))
    }
  }

  def login() = Action { implicit request =>
    val body = request.body.asFormUrlEncoded
    val login = body.get("login_name")(0)
    val password = body.get("password")(0)
    userDao.login(login, password) match {
      case None              => Results.NotFound(views.html.error("Wrong credentials"))
      case Some(loggedLogin) => Redirect(routes.ZxController.index()).withSession("user" -> loggedLogin)
    }
  }

  def logout() = Action { implicit request =>
    Redirect(routes.ZxController.index()).withSession()
  }

  def showRegister() = Action {
    Ok(views.html.register())
  }

  def register() = Action { implicit request =>
    val body = request.body.asFormUrlEncoded
    val login = body.get("login_name")(0)
    val password1 = body.get("password1")(0)
    val password2 = body.get("password2")(0)
    if (password1 != password2) {
      Results.NotFound(views.html.error("Password confirmation mismatch"))
    } else {
      val result = userDao.register(login, password1)
      result match {
        case Success(_) => Redirect(routes.ZxController.index())
        case Failure(_) => Results.NotFound(views.html.error("Cannot register as [" + login + "]"))
      }

    }
  }

  def listUsers() = Action {
    Ok(views.html.listUsers())
  }
}