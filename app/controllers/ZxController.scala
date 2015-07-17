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
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.WebSocket
import game.GameParams
import game.Coord2D
import scala.util.Try

object ZxController extends Controller with LazyLogging {

  val userDao: UserDao = MongoUserDao

  private def getParam(name: String)(implicit request: Request[AnyContent]) =
    request.body.asFormUrlEncoded.get(name)(0)
  
  def index = Action { request =>
    val userName = request.session.get("user")
    Ok(views.html.index(userName))
  }

  def createGame = Action { implicit request =>
    implicit val userName = request.session.get("user")
    val width = Try(getParam("width").toInt)
    val height = Try(getParam("height").toInt)
    val numberOfWalls = Try(getParam("walls").toInt)
    def ok(size: Int) = size>=1 && size<=12
    (width, height, numberOfWalls) match {
      case (Success(w), Success(h), Success(n)) if(ok(w) && ok(h)) =>
        val params = GameParams(Coord2D(h, w), n)
        val gameId = RoomManager.createGame(params)
        Redirect(routes.ZxController.game(gameId))
      case _ =>
        Results.NotFound(views.html.error("Bad game parameters"))
    }
  }

  def game(gameId: String) = Action { implicit request =>
    implicit val userName = request.session.get("user")
    request.session.get("user").map { user =>
      Ok(views.html.game(gameId))
    }.getOrElse {
      Results.NotFound(views.html.error("You need to log in"))
    }
  }

  def gameWs(gameId: String) = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    Future.successful((RoomManager.room(gameId), request.session.get("user")) match {
      case (Some(actorRef), Some(user)) => Right(SeatActor.props(actorRef, user))
      case _                            => Left(Forbidden)
    })
  }

  def again(gameId: String) = Action { request =>
    implicit val userName = request.session.get("user")
    RoomManager.getContinuation(gameId).map { newGameId =>
      Redirect(routes.ZxController.game(newGameId))
    }.getOrElse {
      Results.NotFound(views.html.error("Game " + gameId + " not found"))
    }
  }

  def login() = Action { implicit request =>
    implicit val userName = request.session.get("user")
    val login = getParam("login_name")
    val password = getParam("password")
    userDao.login(login, password) match {
      case None              => Results.NotFound(views.html.error("Wrong credentials"))
      case Some(loggedLogin) => Redirect(routes.ZxController.index()).withSession("user" -> loggedLogin)
    }
  }

  def logout() = Action { implicit request =>
    Redirect(routes.ZxController.index()).withSession()
  }

  def showRegister() = Action { implicit request =>
    val userName = request.session.get("user")
    Ok(views.html.register(userName))
  }

  def register() = Action { implicit request =>
    implicit val userName = request.session.get("user")
    val login = getParam("login_name")
    val password1 = getParam("password1")
    val password2 = getParam("password2")
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

  def listUsers() = Action { implicit request =>
    val userName = request.session.get("user")
    Ok(views.html.listUsers(userName))
  }

  def listGames() = Action { implicit request =>
    val userName = request.session.get("user")
    Ok(views.html.listGames(userName))
  }
}