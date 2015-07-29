package com.example.controllers

import java.time.LocalDateTime

import scala.Left
import scala.Right
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.example.dao.DataAccessLayer
import com.example.game.Coord2D
import com.example.game.Finished
import com.example.game.GameParams
import com.example.game.PlayerA
import com.example.game.PlayerB
import com.example.services.ServicesLayer
import com.example.util.DateTimeUtil.ldtOrdering
import com.typesafe.scalalogging.LazyLogging
import controllers.routes

import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Results
import play.api.mvc.WebSocket

trait ZxController extends Controller with LazyLogging {
  this: DataAccessLayer with ServicesLayer =>

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
    val afterPlay = Try(getParam("afterPlay")).map(_=="on").getOrElse(false)
    val ranking = Try(getParam("rankingGame")).map(_=="on").getOrElse(false)
    def ok(size: Int) = size>=1 && size<=12
    (width, height, numberOfWalls) match {
      case (Success(w), Success(h), Success(n)) if(ok(w) && ok(h)) =>
        val params = GameParams(Coord2D(h, w), n, afterPlay, ranking)
        val gameId = RoomManager.createGame(params)
        Redirect(routes.Application.game(gameId))
      case _ =>
        Results.NotFound(views.html.error("errors.badParameters"))
    }
  }

  def game(gameId: String) = Action { implicit request =>
    implicit val userName = request.session.get("user")
    request.session.get("user").map { user =>
      Ok(views.html.game(gameId))
    }.getOrElse {
      Results.NotFound(views.html.error("errors.needLogIn"))
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
      Redirect(routes.Application.game(newGameId))
    }.getOrElse {
      Results.NotFound(views.html.error("errors.gameNotFound"))
    }
  }

  def login() = Action { implicit request =>
    implicit val userName = request.session.get("user")
    val login = getParam("login_name")
    val password = getParam("password")
    userDao.login(login, password) match {
      case None              => Results.NotFound(views.html.error("errors.wrongCredentials"))
      case Some(loggedLogin) => Redirect(routes.Application.index()).withSession("user" -> loggedLogin)
    }
  }

  def logout() = Action { implicit request =>
    Redirect(routes.Application.index()).withSession()
  }

  def showRegister() = Action { implicit request =>
    val userName = request.session.get("user")
    Ok(views.html.register(userName))
  }

  def register() = Action { implicit request =>
    def isLoginValid(login: String) = "\\S+".r.pattern.matcher(login).matches
    implicit val userName = request.session.get("user")
    
    val login = getParam("login_name")
    val password1 = getParam("password1")
    val password2 = getParam("password2")
    if(!isLoginValid(login)) {
      Results.BadRequest(views.html.error("errors.whitespacesLogin"))
    } else if (password1 != password2) {
      Results.BadRequest(views.html.error("errors.passwordMismatch"))
    } else {
      val result = userDao.register(login, password1)
      result match {
        case Success(_) => Redirect(routes.Application.index())
        case Failure(_) => Results.NotFound(views.html.error("errors.cannotRegister"))
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

  def getUser(name: String) = Action { implicit request =>
    val userGames = gameDao.listGamesByUser(name).map { game=>
      val (myPlayerId, opponentData) =
        if(game.playerA.map(_.id)==Some(name)) (PlayerA, game.playerB)
        else (PlayerB, game.playerA)
      val opponent = opponentData.map(_.id)
      val state = game.state match {
        case Finished(id) if id==myPlayerId => "Won"
        case Finished(id) if id!=myPlayerId => "Lost"
        case _                              => "Ongoing"
      }
      (opponent, state, game.lastActive)
    }.groupBy(_._1)
    val opponents = userGames.map { case (userId, list) =>
      val all = list.size
      val won = list.count(_._2=="Won")
      val lost = list.count(_._2=="Lost")
      val ongoing = list.count(_._2=="Ongoing")
      val lastGame = list.map(_._3).max
      OpponentEntry(userId, all, won, lost, ongoing, Some(lastGame))
    }.toList.sortBy(-_.allGames)
    val total = {
      val all = opponents.map(_.allGames).sum
      val won = opponents.map(_.wonGames).sum
      val lost = opponents.map(_.lostGames).sum
      val ongoing = opponents.map(_.ongoingGames).sum
      val lastGame = opponents.map(_.lastGame.get).reduceOption(List(_,_).max)
      OpponentEntry(None, all, won, lost, ongoing, lastGame)
    }
    val userData = UserData(name,total,opponents)
    
    val watcher = request.session.get("user")
    Ok(views.html.user(userData)(watcher))
  }
}

case class OpponentEntry(
    name: Option[String],
    allGames: Int,
    wonGames: Int,
    lostGames: Int,
    ongoingGames: Int,
    lastGame: Option[LocalDateTime])
case class UserData(
    name: String,
    total: OpponentEntry,
    opponents: List[OpponentEntry])