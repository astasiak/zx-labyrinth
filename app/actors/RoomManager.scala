package actors

import java.util.UUID
import scala.util.Random
import akka.actor.ActorRef
import play.libs.Akka
import akka.actor.Props
import game.GameParams

object RoomManager {

  private val rand = new Random()
  private var rooms = scala.collection.mutable.Map[String, ActorRef]()

  def calculateNewId(): String = {
    val newValue = rand.nextInt(1000000).toString
    if (!rooms.contains(newValue)) newValue
    else calculateNewId()
  }

  def createGame(params: GameParams): String = {
    val id = calculateNewId()
    val gameActor = Akka.system().actorOf(Props(classOf[GameActor],params))
    rooms += id->gameActor
    id
  }
  
  def room(id: String) = {
    rooms.get(id)
  }
}