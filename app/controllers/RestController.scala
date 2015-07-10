package controllers

import com.typesafe.scalalogging.LazyLogging
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.libs.json.Writes
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

object RestController extends Controller with LazyLogging {
  
  val userDao: UserDao = MongoUserDao
  
  def listGames = Action { request =>
    val rooms = RoomManager.listRooms()
    val jsonRooms = rooms.map({room=>
      val GameInfoOMsg(id,GameParams((h,w),walls),playerA,playerB,state) = room
      GameRestModel(id, w, h, walls, playerA, playerB, state.toString)
    })
    Ok(Json.toJson(jsonRooms))
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