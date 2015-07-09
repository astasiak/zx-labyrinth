package dao

import com.mongodb.casbah.Imports._
import util.Properties
import scala.util._

object MongoUserDao extends UserDao {
  val uri = Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/")
  println("Using Mongo URI: ["+uri+"]")
  val mongoUri = MongoClientURI(uri)
  val db = MongoClient(mongoUri)(mongoUri.database.getOrElse("test"))
  val userCollection = db("users")
  
  override def register(user: UserModel): Try[String] = {
    val userObj = UserMongoMapper.mapToMongo(user)
    Try(userCollection.insert(userObj)).map(x=>user.name)
  }
  override def login(user: UserModel): Option[String] = {
    val userObj = UserMongoMapper.mapToMongo(user)
    val foundObj = userCollection.findOne(userObj)
    foundObj.flatMap(UserMongoMapper.mapFromMongo(_)).map(_.name)
  }
  override def remove(name: String): Unit = {
    userCollection -= MongoDBObject("name"->name)
  }
  override def listUsers(): List[UserModel] = {
    val cursor = userCollection.find()
    UserMongoMapper.mapFromMongo(cursor).toList
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
        "password"->entity.password)
  }
  override def mapFromMongo(obj: MongoDBObject): Option[UserModel] = {
    val name = obj.getAs[String]("_id")
    val password = obj.getAs[String]("password") 
    (name, password) match {
      case (Some(n),Some(p)) => Some(UserModel(n,p))
      case _ => None
    }
  }
}

trait UserDao {
  def register(user: UserModel): Try[String]
  def login(user: UserModel): Option[String]
  def remove(id: String): Unit
  def listUsers(): List[UserModel]
}

case class UserModel(name: String, password: String)