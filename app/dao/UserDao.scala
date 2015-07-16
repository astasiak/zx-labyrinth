package dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._
import org.bson.BSON
import util.DateTimeConversions._
import java.util.Date
import java.time.LocalDateTime

object MongoUserDao extends UserDao with LazyLogging {
  
  val uri = Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/")
  logger.debug("Using Mongo URI: ["+uri+"]")
  val mongoUri = MongoClientURI(uri)
  val db = MongoClient(mongoUri)(mongoUri.database.getOrElse("test"))
  val userCollection = db("users")
  
  override def register(login: String, password: String): Try[String] = {
    val userToInsert = UserModel(login,password, LocalDateTime.now, LocalDateTime.now)
    val userObj = UserMongoMapper.mapToMongo(userToInsert)
    Try(userCollection.insert(userObj)).map(x=>login)
  }
  override def login(login: String, password: String): Option[String] = {
    val query = MongoDBObject("_id"->login, "password"->password)
    userCollection.findOne(query) match {
      case None => None
      case Some(_) => {
        touchUser(login)
        Some(login)
      }
    }
  }
  
  override def touchUser(login: String) = {
    userCollection.update(MongoDBObject("_id"->login), $set("lastSeen"->LocalDateTime.now.toDate))
  }
  
  override def remove(login: String): Unit = {
    userCollection -= MongoDBObject("_id"->login)
  }
  override def listUsers(): List[UserModel] = {
    val cursor = userCollection.find()
    UserMongoMapper.mapFromMongo(cursor).toList
  }
  override def dropAllUsers(): Unit = {
    userCollection.drop
  }
}

trait UserDao {
  def register(login: String, password: String): Try[String]
  def login(login: String, password: String): Option[String]
  def touchUser(login: String): Unit
  def remove(login: String): Unit
  def listUsers(): List[UserModel]
  def dropAllUsers: Unit
}
