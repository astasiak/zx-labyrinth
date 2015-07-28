package com.example.dao

import java.time.LocalDateTime
import java.util.Date

import com.mongodb.casbah.Imports.BasicDBObject
import com.mongodb.casbah.Imports.DBObject
import com.mongodb.casbah.Imports.MongoCursor
import com.mongodb.casbah.Imports.MongoDBList
import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.casbah.Imports.wrapDBObj
import com.novus.salat.Context
import com.novus.salat.grater

import com.example.game._
import play.api.Play
import com.example.util.DateTimeUtil.wrapJava8Date
import com.example.util.DateTimeUtil.wrapOldJavaDate

trait MongoMapper[T] {
  def mapToMongo(entity: T): MongoDBObject
  def mapFromMongo(obj: MongoDBObject): Option[T]
  def mapFromMongo(cursor: MongoCursor): Iterator[T] =
    for { x <- cursor;t = mapFromMongo(x) if t!=None }
      yield t.get
}

object UserMongoMapper extends MongoMapper[UserModel] {
  override def mapToMongo(entity: UserModel): MongoDBObject = 
    MongoDBObject(
        "_id"->entity.name,
        "password"->entity.password,
        "lastSeen"->entity.lastSeen.toDate,
        "registered"->entity.registered.toDate)

  override def mapFromMongo(obj: MongoDBObject): Option[UserModel] = {
    val name = obj.getAs[String]("_id")
    val password = obj.getAs[String]("password")
    val defaultDate = LocalDateTime.of(2001,1,1,0,0)
    val lastSeen = obj.getAs[Date]("lastSeen").map(_.toLocalDateTime).getOrElse(defaultDate)
    val registered = obj.getAs[Date]("registered").map(_.toLocalDateTime).getOrElse(defaultDate)
    val rating = obj.getAs[Number]("rating").map(_.intValue).getOrElse(1200)
    (name, password) match {
      case (Some(n),Some(p)) => Some(UserModel(n, p, lastSeen, registered, rating))
      case _ => None
    }
  }
}

object GameMongoMapper extends MongoMapper[GameModel] {
  // workaround for salat grater problem with dynamic classloader
  implicit val ctx = new Context {val name = "Custom_Classloader"}
  ctx.registerClassLoader(Play.classloader(Play.current))
  
  override def mapToMongo(entity: GameModel): MongoDBObject = {
    val params = grater[GameParams].asDBObject(entity.params)
    MongoDBObject(
        "_id"->entity.id,
        "state"->entity.state.toString,
        "playerA"->entity.playerA.map(_.id),
        "playerB"->entity.playerB.map(_.id),
        "params"->params,
        "lastActive"->entity.lastActive.toDate,
        "created"->entity.created.toDate)
  }
  
  override def mapFromMongo(obj: MongoDBObject): Option[GameModel] = {
    val id = obj.getAs[String]("_id")
    val stateRaw = obj.getAs[String]("state")
    val state = Map[String, GameState]("Awaiting"->Awaiting,
        "Ongoing(PlayerA)"->Ongoing(PlayerA), "Ongoing(PlayerB)"->Ongoing(PlayerB),
        "Finished(PlayerA)"->Finished(PlayerA), "Finished(PlayerB)"->Finished(PlayerB))
      .get(stateRaw.getOrElse(""))
    val playerA = obj.getAs[String]("playerA")
    val playerB = obj.getAs[String]("playerB")
    val boardA = obj.getAs[DBObject]("boardA").map(mapBoardFromMongo(_))
    val boardB = obj.getAs[DBObject]("boardB").map(mapBoardFromMongo(_))
    val params = obj.getAs[DBObject]("params")
    val defaultDate = LocalDateTime.of(2001,1,1,0,0)
    val created = obj.getAs[Date]("created").map(_.toLocalDateTime).getOrElse(defaultDate)
    val lastActive = obj.getAs[Date]("lastActive").map(_.toLocalDateTime).getOrElse(defaultDate)
    (id, params, state) match {
      case (Some(id), Some(params), Some(state)) =>
        Some(
            GameModel(id, grater[GameParams].asObject(params), state,
                playerA.map(GamePlayerModel(_, boardA)),
                playerB.map(GamePlayerModel(_, boardB)),
                lastActive, created))
      case _ => None
    }
  }
  
  // FIXME: Salat 1.9.9 does not support nested collections (like in Board) :-(
  //def mapBoardToMongo(board: Board): MongoDBObject = grater[Board].asDBObject(board)
  //def mapBoardFromMongo(obj: MongoDBObject): Board = grater[Board].asObject(obj)
  
  // TODO: do it better! (and also for json mapping trough websocket)
  def mapBoardToMongo(board: Board): MongoDBObject = {
    def map(coord: Coord2D) = {MongoDBObject("x"->coord.x,"y"->coord.y)}
    def bordersString(borders: Vector[Vector[Border]]) =
      borders.map({ row=>
        row.map(_ match {
          case Border(true,true) => "A"
          case Border(true,false) => "B"
          case Border(false,true) => "C"
          case Border(false,false) => "D"
        }).mkString
      }).mkString("|")
    MongoDBObject(
        "start"->map(board.start),
        "meta"->map(board.meta),
        "size"->map(board.size),
        "position"->map(board.position),
        "history"->board.history.map(map(_)).toList,
        "bordersH"->bordersString(board.borders.horizontal),
        "bordersV"->bordersString(board.borders.vertical))
  }
  def getCoord(obj: MongoDBObject) = Coord2D(obj.getAs[Int]("x").get, obj.getAs[Int]("y").get)
  def mapBoardFromMongo(obj: MongoDBObject): Board = {
    val start = getCoord(obj.getAs[DBObject]("start").get)
    val meta = getCoord(obj.getAs[DBObject]("meta").get)
    val position = getCoord(obj.getAs[DBObject]("position").get)
    val size = getCoord(obj.getAs[DBObject]("size").get)
    val history = obj.getAs[MongoDBList]("history").get.map({ obj =>
      getCoord(obj.asInstanceOf[BasicDBObject])
    }).toList
    def makeBorders(bordersString: String) = bordersString.split('|').map { row=>
      row.map( _ match {
        case 'A' => Border(true,true)
        case 'B' => Border(true,false)
        case 'C' => Border(false,true)
        case 'D' => Border(false,false)
      }).toVector
    }.toVector
    val bordersH = makeBorders(obj.getAs[String]("bordersH").get)
    val bordersV = makeBorders(obj.getAs[String]("bordersV").get)
    Board(size, position, start, meta, Borders(bordersV, bordersH), history)
  }
}
/*
object HistoryListTransformer extends CustomTransformer[List[Coord2D], DBObject] {
  def deserialize(l: DBObject) = List[Coord2D]()
  def serialize(lc: List[Coord2D]) = new BasicDBObject()
}

object BordersTransformer extends CustomTransformer[Borders, DBObject] {
  def deserialize(l: DBObject) = Borders(Vector[Vector[Border]](),Vector[Vector[Border]]())
  def serialize(vvb: Borders) = new BasicDBObject()
}*/