package controllers

import com.example.controllers._
import com.example.dao.MongoDataAccessLayer
import com.example.services.ServicesLayer

object Application
  extends ControllersLayer
  with ServicesLayer
  with MongoDataAccessLayer {
}