package controllers

import controllers.action.{AdminPermission, LoggedIn, OfficerPermission}
import play.api.Logger
import play.api.libs.Files
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import repository.{PackageRepository, PackageStatus}
import services.SmsSender
import util.VerifyCodeGenerator

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Using}


@Singleton
class PackageController @Inject()(cc: ControllerComponents,
                                  admin: AdminPermission,
                                  officerPermission: OfficerPermission,
                                  loggedIn: LoggedIn,
                                  smsSender: SmsSender,
                                  packageRepository: PackageRepository) extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  def create(campaignId: Int): Action[MultipartFormData[Files.TemporaryFile]] = admin(parse.multipartFormData) { request =>
    request.body.file("packages") match {
      case Some(file) =>
        val (valid, invalid) = CreatePackage.importFromFile(file.ref, campaignId).partition(_.isValid)
        invalid.foreach(println)
        valid.map(packageRepository.create)
        Ok
      case None =>
        BadRequest
    }
  }

  def packageByStatus(campaignId: Int, status: Int): Action[AnyContent] = loggedIn(parse.anyContent){ _ =>
    Ok(Json.toJson(packageRepository.packagesByStatus(campaignId, PackageStatus(status))))
  }

  def allPackages(campaignId: Int): Action[AnyContent] = loggedIn(parse.anyContent){ _ =>
    Ok(Json.toJson(packageRepository.allPackages(campaignId)))
  }
  def confirmReceivingPackage(code: String): Action[AnyContent] = officerPermission(parse.anyContent) {request =>
     packageRepository.packageByVerifyCode(code) match {
       case None =>
         NotFound(s"package with code $code not found")
       case Some(pkage) =>
         packageRepository.confirmReceivingPackage(pkage.phone, code, request.username)
         Ok(Json.toJson(pkage))
     }
  }

  def initVerifyCode: Action[List[InitVerifyCode]] = officerPermission(parse.json[List[InitVerifyCode]]){request =>
    if (request.username == "admin"){
       NotAcceptable("only officer account can submit")
    } else {
       VerifyCodeGenerator.genCodes(request.body.length, packageRepository.verifyCodes) zip request.body foreach{case(code, initVerifyCode) =>
         smsSender.send(initVerifyCode.phone, initVerifyCode.note).map {_ =>
           packageRepository.initVerifyCode(code, initVerifyCode.phone, request.username, initVerifyCode.note) match {
             case 1 =>
             case _ =>
               logger.debug(s"phone number: ${initVerifyCode.phone} not found")
           }
         }
       }
      Ok
    }
  }

  def resendVerifyCode: Action[InitVerifyCode]  = officerPermission(parse.json[InitVerifyCode]).async{ request =>
    val newCode = VerifyCodeGenerator.next(packageRepository.verifyCodes)
    if (request.username == "admin"){
       Future.successful(NotAcceptable("only officer account can resend"))
    } else {
      smsSender.send(request.body.phone, request.body.note) map { _ =>
        packageRepository.updateVerifyCode(newCode, request.body.phone, request.username) match {
          case 1 =>
            Ok
          case _ =>
            NotFound(s"phone number: ${request.body.phone} not found")
        }
      }
    }
  }
}

case class CreatePackage(name: String, phone: String, campaignId: Int) {
  def isValid: Boolean = phone.forall(_.isDigit)

  override def toString: String = {
    if (isValid) super.toString else s"invalid ${this.phone}"
  }
}

object CreatePackage {
  def importFromFile(file: Files.TemporaryFile, campaignId: Int): List[CreatePackage] = {
    Using(Source.fromFile(file)) { bufferedSource =>
      bufferedSource.getLines().map { line =>
        val cols = line.split(",").map(_.trim)
        CreatePackage(cols(0), cols(1), campaignId)
      }.toList
    } match {
      case Success(value) => value
      case Failure(exception) =>
        exception.printStackTrace()
        Nil
    }
  }
}

case class InitVerifyCode(phone: String, note: String)

object InitVerifyCode{
  implicit val fmt: OFormat[InitVerifyCode] = Json.format[InitVerifyCode]
}