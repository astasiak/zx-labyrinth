package controllers

import com.typesafe.scalalogging.LazyLogging
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import game.GameParams
import actors.RoomManager
import actors.messages.GameInfoOMsg
import dao.MongoUserDao
import dao.UserDao
import dao.UserModel
import rest.User._
import rest.Game._
import dao.MongoGameDao
import dao.GameDao
import dao.GameModel
import game.Coord2D

object RestController extends Controller with LazyLogging {
  
  val userDao: UserDao = MongoUserDao
  val gameDao: GameDao = MongoGameDao
  
  def listGames = Action { request =>
    val games = gameDao.listGames
    val memoryGames = RoomManager.memoryGames
    val jsonGames = games.map({game=>
      val GameModel(id,GameParams(Coord2D(h,w),walls), state, playerA, playerB, lastActive, created) = game
      val inMemory = memoryGames.contains(id)
      GameRestModel(id, w, h, walls, playerA.map(_.id), playerB.map(_.id), state.toString, lastActive, created, inMemory)
    })
    Ok(Json.toJson(jsonGames))
  }
  
  def listUsers = Action { request =>
    val users = userDao.listUsers
    val jsonUsers = users.map({user=>
      val UserModel(name, _, lastSeen, registered) = user
      UserRestModel(name, lastSeen, registered)
    })
    Ok(Json.toJson(jsonUsers))
  }
}