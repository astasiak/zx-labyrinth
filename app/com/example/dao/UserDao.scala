package com.example.dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._
import org.bson.BSON
import java.util.Date
import com.example.util.DateTimeUtil._
import com.example.util.PasswordHasher

class MongoUserDao(db: MongoDB) extends UserDao with LazyLogging {
  
  val userCollection = db("users")
  
  override def register(login: String, password: String): Try[String] = {
    val hash = PasswordHasher.hash(password)
    val userToInsert = UserModel(login, hash, now, now, INIT_RATING)
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
    userCollection.update(MongoDBObject("_id"->login), $set("lastSeen"->now.toDate))
  }
  
  override def getUser(login: String) = {
    val obj = userCollection.findOneByID(login)
    obj.flatMap(UserMongoMapper.mapFromMongo(_))
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
  
  override def alterRating(login: String, ratingChange: Int): Unit = {
    userCollection.update(MongoDBObject("_id"->login),$inc("rating"->ratingChange))
  }
  
  override def setRating(login: String, rating: Int): Unit = {
    userCollection.update(MongoDBObject("_id"->login),$set("rating"->rating))
  }
}

trait UserDao {
  val INIT_RATING = 1200
  
  def register(login: String, password: String): Try[String]
  def login(login: String, password: String): Option[String]
  def touchUser(login: String): Unit
  def getUser(login: String): Option[UserModel]
  def remove(login: String): Unit
  def listUsers(): List[UserModel]
  def dropAllUsers: Unit
  def alterRating(login: String, ratingChange: Int): Unit
  def setRating(login: String, rating: Int): Unit
}
