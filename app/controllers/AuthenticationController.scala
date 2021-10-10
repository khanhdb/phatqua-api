package controllers

import play.api.libs.json.{Json, Reads}
import play.api.mvc._
import services.UserAuthenticator

import javax.inject._

@Singleton
class AuthenticationController @Inject()(cc: ControllerComponents, userAuthenticator: UserAuthenticator) extends AbstractController(cc) {
  def login: Action[Login] = Action(parse.json[Login]){request =>
    val data = request.body
     if (userAuthenticator.validate(data.username, data.password)){
       Ok.withSession("username" -> data.username)
     } else {
       Unauthorized("Sai username hoặc password")
     }
  }
}


case class Login(username: String, password: String)

object Login{
  implicit val fmt: Reads[Login] = Json.format[Login]
}
