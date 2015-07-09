package controllers

import com.typesafe.scalalogging.LazyLogging
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json.Json
import game.GameParams
import actors.RoomManager
import actors.messages.GameInfoOMsg
import dao.MongoUserDao
import dao.UserDao

object RestController extends Controller with LazyLogging {
  
  case class GameRestModel(id: String, width: Int, height: Int, walls: Int, playerA: Option[String], playerB: Option[String], state: String)
  implicit val gamesWrites = Json.writes[GameRestModel]
  
  case class UserRestModel(name: String)
  implicit val usersWrites = Json.writes[UserRestModel]
  
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
      UserRestModel(user.name)
    })
    Ok(Json.toJson(jsonUsers))
  }
}