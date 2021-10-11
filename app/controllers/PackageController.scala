package controllers

import controllers.action.{Admin, Authenticated}
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc._
import repository.PackageRepository

import javax.inject._
import scala.io.Source
import scala.util.{Failure, Success, Using}


@Singleton
class PackageController @Inject()(cc: ControllerComponents,
                                  admin: Admin,
                                  authenticatd: Authenticated,
                                  packageRepository: PackageRepository) extends AbstractController(cc) {

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

  def allPackages: Action[AnyContent] = authenticatd(parse.anyContent){ _ =>
    Ok(Json.toJson(packageRepository.allPackages))
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