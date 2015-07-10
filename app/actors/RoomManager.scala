package actors

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

import game.GameParams
import actors.messages.AskForGameInfoIMsg
import actors.messages.GameInfoOMsg

object RoomManager {

  private val rand = new Random()
  private val rooms = scala.collection.mutable.Map[String, (GameParams, ActorRef)]()
  private val continuations = scala.collection.mutable.Map[String, String]()

  def getContinuation(oldId: String): Option[String] =
    rooms get oldId map { oldRoom =>
    continuations get oldId match {
      case Some(newId) => newId
      case None => {
        val newId = createGame(oldRoom._1)
        continuations += oldId->newId
        newId
      }
    }
  }
  
  def calculateNewId(): String = {
    val newValue = rand.nextInt(1000000).toString
    if (!rooms.contains(newValue)) newValue
    else calculateNewId()
  }

  def createGame(params: GameParams): String = {
    val id = calculateNewId()
    val gameActor = Akka.system().actorOf(Props(classOf[GameActor],id,params))
    rooms += id->(params, gameActor)
    id
  }
  
  def room(id: String) = rooms.get(id).map(_._2)
  
  implicit val timeout: Timeout = Timeout(5 seconds)
  def listRooms() = rooms.values.map({ case (_,actor) =>
      actor ? AskForGameInfoIMsg()
  }).map({ Await.result(_, timeout.duration).asInstanceOf[GameInfoOMsg]
  })
}