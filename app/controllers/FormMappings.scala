package controllers

import play.api.mvc.Request
import play.api.mvc.AnyContent
import game.GameParams
import game.Coord2D

object FormMappings {

  implicit def mapPostToGameParameters(request: Request[AnyContent]): GameParams = {
    val form = request.body.asFormUrlEncoded
    val width = form.get("width")(0).toInt
    val height = form.get("height")(0).toInt
    val numberOfWalls = form.get("walls")(0).toInt
    GameParams(Coord2D(height, width), numberOfWalls)
  }

}