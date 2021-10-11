package controllers

import controllers.action.AdminPermission
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import repository.{Password, UserRepository, Username}

import javax.inject._
import scala.util.{Failure, Success}


@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               admin: AdminPermission,
                               userRepository: UserRepository) extends AbstractController(cc) {

  def createOfficer: Action[Officer] = admin(parse.json[Officer]) { request =>
    val officer = request.body
    userRepository.createOfficer(Username(officer.username), Password(officer.password), officer.fullName) match {
      case Success(_) =>
        Ok(s"created officer account")
      case Failure(exception) =>
        exception.printStackTrace()
        NotModified
    }
  }

  def deleteOfficer(username: String): Action[AnyContent] = admin(parse.anyContent){_ =>
    userRepository.deleteOfficer(Username(username)) match {
      case 1 => Ok(s"deleted officer $username")
      case _ => NotModified
    }
  }

}

case class Officer(username: String, password: String, fullName: String)

object Officer {
  implicit val fmt: OFormat[Officer] = Json.format[Officer]
}