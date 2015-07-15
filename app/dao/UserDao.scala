package dao

import com.mongodb.casbah.Imports._
import scala.util._
import java.time.LocalDateTime
import org.bson.BSON
import util.DateTimeConversions._
import util.PasswordHasher
import java.util.Date

object MongoUserDao extends UserDao {
  
  val uri = Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/")
  println("Using Mongo URI: ["+uri+"]")
  val mongoUri = MongoClientURI(uri)
  val db = MongoClient(mongoUri)(mongoUri.database.getOrElse("test"))
  val userCollection = db("users")
  
  override def register(login: String, password: String): Try[String] = {
    val hash = PasswordHasher.hash(password)
    val userToInsert = UserModel(login, hash, LocalDateTime.now, LocalDateTime.now)
    val userObj = UserMongoMapper.mapToMongo(userToInsert)
    Try(userCollection.insert(userObj)).map(x=>login)
  }
  override def login(login: String, password: String): Option[String] = {
    val isOk = userCollection.findOneByID(login)
      .flatMap(mongoObj=> mongoObj.getAs[String]("password"))
      .map(hash=> PasswordHasher.check(password,hash))
      .getOrElse(false)
    if(isOk) Some(login) else None
  }
  
  override def touchUser(login: String) = {
    userCollection.update(MongoDBObject("_id"->login), $set("lastSeen"->LocalDateTime.now.toDate))
  }
  
  override def remove(name: String): Unit = {
    userCollection -= MongoDBObject("name"->name)
  }
  override def listUsers(): List[UserModel] = {
    val cursor = userCollection.find()
    UserMongoMapper.mapFromMongo(cursor).toList
  }
  override def dropAllUsers(): Unit = {
    userCollection.drop
  }
}

trait MongoMapper[T] {
  def mapToMongo(entity: T): MongoDBObject
  def mapFromMongo(obj: MongoDBObject): Option[T]
  def mapFromMongo(cursor: MongoCursor): Iterator[T] =
    for { x <- cursor;t = mapFromMongo(x) if t!=None }
      yield t.get
}

object UserMongoMapper extends MongoMapper[UserModel] {
  override def mapToMongo(entity: UserModel): MongoDBObject = { 
    MongoDBObject(
        "_id"->entity.name,
        "password"->entity.password,
        "lastSeen"->entity.lastSeen.toDate,
        "registered"->entity.registered.toDate)
  }
  override def mapFromMongo(obj: MongoDBObject): Option[UserModel] = {
    val name = obj.getAs[String]("_id")
    val password = obj.getAs[String]("password") 
    val lastSeen = obj.getAs[Date]("lastSeen")
    val registered = obj.getAs[Date]("registered")
    (name, password, lastSeen, registered) match {
      case (Some(n),Some(p),Some(ls),Some(cr)) => Some(UserModel(n,p,ls.toLocalDateTime,cr.toLocalDateTime)) // TODO: ?
      case (Some(n),Some(p),_,_) => Some(UserModel(n,p,LocalDateTime.now,LocalDateTime.now))
      case _ => None
    }
  }
}

trait UserDao {
  def register(login: String, password: String): Try[String]
  def login(login: String, password: String): Option[String]
  def touchUser(login: String): Unit
  def remove(id: String): Unit
  def listUsers(): List[UserModel]
  def dropAllUsers: Unit
}

case class UserModel(name: String, password: String, lastSeen: LocalDateTime, registered: LocalDateTime)
