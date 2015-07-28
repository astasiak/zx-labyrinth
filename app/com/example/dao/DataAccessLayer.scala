package com.example.dao

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import scala.util._


trait DataAccessLayer {
  val userDao: UserDao
  val gameDao: GameDao
}

trait MongoDataAccessLayer extends DataAccessLayer with LazyLogging {
  val uri = Properties.envOrElse("MONGOLAB_URI", "mongodb://localhost:27017/")
  logger.debug("Using Mongo URI: ["+uri+"]")
  val mongoUri = MongoClientURI(uri)
  val db = MongoClient(mongoUri)(mongoUri.database.getOrElse("test"))
  
  val userDao = new MongoUserDao(db)
  val gameDao = new MongoGameDao(db)
}