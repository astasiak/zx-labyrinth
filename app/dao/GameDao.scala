package dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._
import org.bson.BSON
import util.DateTimeConversions._
import java.util.Date
import java.time.LocalDateTime
import game._

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
    val cursor = gameCollection.find()
    GameMongoMapper.mapFromMongo(cursor).toList
  }
  override def dropAllGames(): Unit = {
    gameCollection.drop
  }
  override def updateGameState(id: String, gameState: GameState) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("state"->gameState.toString))
  }
  override def updatePlayers(id: String, playerA: Option[String], playerB: Option[String]) = {
    gameCollection.update(MongoDBObject("_id"->id), $set("playerA"->playerA, "playerB"->playerB))
  }
  override def updateBoard(id: String, playerId: PlayerId, board: Board) = {
    val mongoBoard = GameMongoMapper.mapBoardToMongo(board)
    val updater = playerId match {
      case PlayerA => $set("boardA"->mongoBoard)
      case PlayerB => $set("boardB"->mongoBoard)
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
