package dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._
import org.bson.BSON
import util.DateTimeConversions._
import java.util.Date
import java.time.LocalDateTime
import game._
import util.DateTimeConversions._

object MongoGameDao extends GameDao with LazyLogging {
  
  val uri = Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/")
  logger.debug("Using Mongo URI: ["+uri+"]")
  val mongoUri = MongoClientURI(uri)
  val db = MongoClient(mongoUri)(mongoUri.database.getOrElse("test"))
  val gameCollection = db("games")
  
  override def saveGame(game: GameModel): Unit = {
    val gameObj = GameMongoMapper.mapToMongo(game)
    Try(gameCollection.insert(gameObj))
  }
  
  override def remove(id: String): Unit = {
    gameCollection -= MongoDBObject("_id"->id)
  }
  override def listGames(): List[GameModel] = {
    val cursor = gameCollection.find(MongoDBObject(),
        MongoDBObject("id"->1, "state"->1, "params"->1, "playerA"->1, "playerB"->1,"created"->1,"lastActive"->1))
    GameMongoMapper.mapFromMongo(cursor).toList
  }
  override def dropAllGames(): Unit = {
    gameCollection.drop
  }
  override def updateGameState(id: String, gameState: GameState) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("state"->gameState.toString, "lastActive"->LocalDateTime.now.toDate))
  }
  override def updatePlayers(id: String, playerA: Option[String], playerB: Option[String]) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("playerA"->playerA, "playerB"->playerB, "lastActive"->LocalDateTime.now.toDate))
  }
  override def updateBoard(id: String, playerId: PlayerId, board: Board) = {
    val mongoBoard = GameMongoMapper.mapBoardToMongo(board)
    val updater = playerId match {
      case PlayerA => $set("boardA"->mongoBoard, "lastActive"->LocalDateTime.now.toDate)
      case PlayerB => $set("boardB"->mongoBoard, "lastActive"->LocalDateTime.now.toDate)
    }
    gameCollection.update(MongoDBObject("_id"->id), updater)
  }
  override def loadGame(id: String) = {
    val gameModelDB = gameCollection.findOneByID(id)
    val gameModel = gameModelDB.flatMap(GameMongoMapper.mapFromMongo(_))
    gameModel.map { model =>
      val game = new Game(model.params)
      game.gameState = model.state
      val dataA = model.playerA.map(data=>(data.id,data.board))
      val dataB = model.playerB.map(data=>(data.id,data.board))
      game.putPlayers(dataA, dataB)
      game
    }
  }
}

trait GameDao {
  def saveGame(game: GameModel): Unit
  def remove(id: String): Unit
  def listGames(): List[GameModel]
  def dropAllGames: Unit
  def updateGameState(id: String, gameState: GameState): Unit
  def updatePlayers(id: String, playerA: Option[String], playerB: Option[String]): Unit
  def updateBoard(id: String, playerId: PlayerId, board: Board): Unit
  def loadGame(id: String): Option[Game]
}
