package com.example.services

import java.util.UUID
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import play.libs.Akka
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.AskableActorRef
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.example.game.Game
import com.example.game.GameParams
import com.example.services.messages.AskForGameInfoIMsg
import com.example.services.messages.GameInfoOMsg
import com.example.dao.MongoGameDao
import com.typesafe.scalalogging.LazyLogging
import com.example.dao.DataAccessLayer

trait RoomManagerComponent {
  this: DataAccessLayer with GameActorComponent =>

  object RoomManager extends LazyLogging {
  
    private val rand = new Random()
    private val rooms = scala.collection.mutable.Map[String, (GameParams, ActorRef)]()
    private val continuations = scala.collection.mutable.Map[String, String]()
  
    def createGame(params: GameParams): String = {
      val id = calculateNewId()
      logger.info(s"Creating new game with params ${params} and id:${id}")
      val gameActor = Akka.system().actorOf(Props(classOf[GameActor], RoomManagerComponent.this, id, params))
      rooms += id->(params, gameActor)
      id
    }
    
    private def loadGame(id: String) = {
      val game: Option[Game] = gameDao.loadGame(id)
      logger.info(s"Loading game with id:${id} for datastore - success:${game!=None}")
      game.map { game =>
        val gameActor = Akka.system().actorOf(Props(classOf[GameActor], RoomManagerComponent.this, id, game))
        rooms += id->(game.params, gameActor)
        gameActor
      }
    }
    
    def room(id: String) = {
      val memoryGame = rooms.get(id).map(_._2)
      memoryGame orElse loadGame(id)
    }
    
    def memoryGames: Set[String] = rooms.keySet.toSet
  
    def getContinuation(oldId: String): Option[String] =
      continuations get oldId match {
        case Some(newId) => Some(newId)
        case None => {
          rooms get oldId map { oldRoom =>
            logger.info(s"Creating continuation for the game with id:${oldId}")
            val newId = createGame(oldRoom._1)
            continuations += oldId->newId
            newId
          }
        }
      }
    
    private def calculateNewId(): String = {
      val newValue = rand.nextInt(1000000).toString
      if (!rooms.contains(newValue)) newValue
      else calculateNewId()
    }
  }
}