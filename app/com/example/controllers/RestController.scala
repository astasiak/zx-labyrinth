package com.example.controllers

import com.example.controllers.rest.Game.GameRestModel
import com.example.controllers.rest.Game.gamesWrites
import com.example.controllers.rest.User.UserRestModel
import com.example.controllers.rest.User.usersWrites
import com.example.dao.DataAccessLayer
import com.example.dao.GameModel
import com.example.dao.UserModel
import com.example.game.Coord2D
import com.example.game.Finished
import com.example.game.GameParams
import com.example.services.ServicesLayer
import com.typesafe.scalalogging.LazyLogging

import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller

trait RestController extends Controller with LazyLogging {
  this: DataAccessLayer with ServicesLayer =>
  
  def restGames = Action { request =>
    val limit = request.queryString.get("limit").flatMap(_.headOption).map(_.toInt)
    val userId = request.queryString.get("user").flatMap(_.headOption)
    val games = gameDao.listGames(userId, limit)
    val memoryGames = RoomManager.memoryGames
    val jsonGames = games.map({game=>
      val GameModel(id,GameParams(Coord2D(h,w),walls, afterFinish, ranking), state, playerA, playerB, lastActive, created) = game
      val inMemory = memoryGames.contains(id)
      GameRestModel(id, w, h, walls, ranking, playerA.map(_.id), playerB.map(_.id), state.toString, lastActive, created, inMemory)
    })
    Ok(Json.toJson(jsonGames))
  }
  
  def restUsers = Action { request =>
    val users = userDao.listUsers
    val games = gameDao.listGames(None, None)
    def getPlayers(games: List[GameModel]) = games
      .flatMap(game=>List(game.playerA.map(_.id), game.playerB.map(_.id)))
      .groupBy(identity).mapValues(_.size)
      .withDefaultValue(0)
    val guser = getPlayers(games)
    val fusers = getPlayers(games.filter(_.state.isInstanceOf[Finished]))
    val jsonUsers = users.map({user=>
      val UserModel(name, _, lastSeen, registered, rating) = user
      UserRestModel(name, lastSeen, registered, fusers(Some(name)), guser(Some(name)), user.rating)
    })
    Ok(Json.toJson(jsonUsers))
  }
}