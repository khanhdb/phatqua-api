package controllers

import controllers.action.AdminPermission
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import repository.CampaignRepository

import javax.inject._


@Singleton
class CampaignController @Inject()(cc: ControllerComponents,
                                   admin: AdminPermission,
                                   campaignRepository: CampaignRepository) extends AbstractController(cc) {

  def create: Action[CreateCampaign] = admin(parse.json[CreateCampaign]) { request =>
    campaignRepository.create(request.body) match {
      case Some(id) =>
        Ok(s"created new campaign with id $id")
      case None =>
        NotModified
    }
  }

  def allCampaign: Action[CreateCampaign] = admin(parse.json[CreateCampaign]) { request =>
    campaignRepository.create(request.body) match {
      case Some(id) =>
        Ok(s"created new campaign with id $id")
      case None =>
        NotModified
    }
  }

  def delete(id: Int): Action[AnyContent] = admin(parse.anyContent){_ =>
    campaignRepository.delete(id) match {
      case 1 =>
        Ok(s"deleted campaign with id $id")
      case _ =>
        NotModified
    }
  }
}

case class CreateCampaign(name: String, location: String)

object CreateCampaign{
  implicit val fmt: OFormat[CreateCampaign] = Json.format[CreateCampaign]
}