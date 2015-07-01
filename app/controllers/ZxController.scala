package controllers

import scala.Left
import scala.concurrent.Future
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import play.api.mvc.Request
import play.api.mvc.AnyContent
import actors.RoomManager
import actors.SeatActor
import play.api.mvc.Results

object ZxController extends Controller {
  
  import FormMappings._
  
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

  def gameWs(gameId: String) = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    Future.successful((RoomManager.room(gameId),request.session.get("user")) match {
      case (Some(actorRef), Some(user)) => Right(SeatActor.props(actorRef, user))
      case _ => Left(Forbidden)
    })
  }
  
  def again(gameId: String) = Action {
    RoomManager.getContinuation(gameId).map { newGameId =>
      Redirect(routes.ZxController.game(newGameId))
    }.getOrElse {
      Results.NotFound(views.html.error("Game "+gameId+" not found"))
    }
  }
  
  def login() = Action { implicit request =>
    val login = request.body.asFormUrlEncoded.get("login_name")(0)
    Redirect(routes.ZxController.index()).withSession("user" -> login)
  }
  
  def logout() = Action { implicit request =>
    Redirect(routes.ZxController.index()).withSession()
  }
}