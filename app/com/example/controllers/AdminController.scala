package com.example.controllers

import com.example.dao.DataAccessLayer
import com.example.services.ServicesLayer
import com.example.util.PasswordHasher
import com.typesafe.scalalogging.LazyLogging

import akka.actor.actorRef2Scala
import play.Play
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Results

trait AdminController extends Controller with LazyLogging {
  this: DataAccessLayer with ServicesLayer =>

  val secretAdminKeyHash = Play.application().configuration().getString("secretAdminKeyHash")
  
  def adminDropUsers = securedAction {
    userDao.dropAllUsers
    Ok("OK")
  }
  
  def adminRecalculateRatings = securedAction { 
    RatingActor.instance ! RatingActor.RecalculateAllHistory
    Ok("OK")
  }
  
  private def securedAction(action: => Result) = Action { request =>
    request.headers.get("secret") match {
      case Some(requestSecret) if PasswordHasher.check(requestSecret, secretAdminKeyHash) => action
      case _ => Results.Forbidden("You are not allowed to do this!")
    }
  }
}