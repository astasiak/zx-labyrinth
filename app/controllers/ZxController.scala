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

object ZxController extends Controller {
  
  import FormMappings._
  
  def index = Action {
    Ok(views.html.index())
  }
  
  def createGame = Action { request =>
    val gameId = RoomManager.createGame(request)
    Redirect(routes.ZxController.game(gameId))
  }
  
  def game(gameId: String) = Action { implicit request =>
    Ok(views.html.game(gameId))
  }

  def gameWs(gameId: String) = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
    Future.successful(RoomManager.room(gameId) match {
      case None => Left(Forbidden)
      case Some(actorRef) => Right(SeatActor.props(actorRef))
    })
  }
}