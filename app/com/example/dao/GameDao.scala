package com.example.dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._
import org.bson.BSON
import java.util.Date
import com.example.game._
import com.example.util.DateTimeUtil._

class MongoGameDao(db: MongoDB) extends GameDao with LazyLogging {
  
  val gameCollection = db("games")
  
  override def saveGame(game: GameModel): Unit = {
    val gameObj = GameMongoMapper.mapToMongo(game)
    Try(gameCollection.insert(gameObj))
  }
  
  override def remove(id: String): Unit = {
    gameCollection -= MongoDBObject("_id"->id)
  }
  
  override def listGames() =
    find(MongoDBObject())
    
  override def listGamesByUser(userId: String) =
    find($or(List(MongoDBObject("playerA"->userId), MongoDBObject("playerB"->userId))))
    
  override def dropAllGames(): Unit = {
    gameCollection.drop
  }
  
  override def updateGameState(id: String, gameState: GameState) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("state"->gameState.toString, "lastActive"->now.toDate))
  }
  
  override def updatePlayers(id: String, playerA: Option[String], playerB: Option[String]) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("playerA"->playerA, "playerB"->playerB, "lastActive"->now.toDate))
  }
  
  override def updateBoard(id: String, playerId: PlayerId, board: Board) = {
    val mongoBoard = GameMongoMapper.mapBoardToMongo(board)
    val updater = playerId match {
      case PlayerA => $set("boardA"->mongoBoard, "lastActive"->now.toDate)
      case PlayerB => $set("boardB"->mongoBoard, "lastActive"->now.toDate)
    }
    gameCollection.update(MongoDBObject("_id"->id), updater)
  }
  
  override def loadGame(id: String) = {
    val gameModelDB = gameCollection.findOneByID(id)
    val gameModel = gameModelDB.flatMap(GameMongoMapper.mapFromMongo(_))
    gameModel.map { model =>
      val game = new Game(model.params)
      game.gameState = model.state
      val dataA = model.playerA.map(data=>(data.id, data.board))
      val dataB = model.playerB.map(data=>(data.id, data.board))
      game.putPlayers(dataA, dataB)
      game
    }
  }
  
  private def find(query: MongoDBObject) = {
    val cursor = gameCollection.find(query,
        MongoDBObject("id"->1, "state"->1, "params"->1, "playerA"->1, "playerB"->1, "created"->1, "lastActive"->1))
    GameMongoMapper.mapFromMongo(cursor).toList
  }
}

trait GameDao {
  def saveGame(game: GameModel): Unit
  def remove(id: String): Unit
  def listGames(): List[GameModel]
  def listGamesByUser(userId: String): List[GameModel]
  def dropAllGames: Unit
  def updateGameState(id: String, gameState: GameState): Unit
  def updatePlayers(id: String, playerA: Option[String], playerB: Option[String]): Unit
  def updateBoard(id: String, playerId: PlayerId, board: Board): Unit
  def loadGame(id: String): Option[Game]
}
