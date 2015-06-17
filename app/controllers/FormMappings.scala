package controllers

import play.api.mvc.Request
import play.api.mvc.AnyContent
import game.GameParams

object FormMappings {

  implicit def mapPostToGameParameters(request: Request[AnyContent]): GameParams = {
    val form = request.body.asFormUrlEncoded
    val width = form.get("width")(0).toInt
    val height = form.get("height")(0).toInt
    val numberOfWalls = form.get("walls")(0).toInt
    GameParams((width, height), numberOfWalls)
  }

}