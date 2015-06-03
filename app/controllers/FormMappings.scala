package controllers

import actors.GameParameters
import play.api.mvc.Request
import play.api.mvc.AnyContent

object FormMappings {

  implicit def mapPostToGameParameters(request: Request[AnyContent]): GameParameters = {
    val form = request.body.asFormUrlEncoded
    val size = form.get("size")(0).toInt
    val numberOfWalls = form.get("walls")(0).toInt
    GameParameters(size, numberOfWalls)
  }

}