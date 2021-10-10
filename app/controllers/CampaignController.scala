package controllers

import controllers.action.Admin
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import repository.CampaignRepository

import javax.inject._


@Singleton
class CampaignController @Inject()(cc: ControllerComponents,
                               admin: Admin,
                               campaignRepository: CampaignRepository) extends AbstractController(cc) {

  def create: Action[Campaign] = admin(parse.json[Campaign]) { request =>
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

case class Campaign(name: String, location: String)

object Campaign{
  implicit val fmt: OFormat[Campaign] = Json.format[Campaign]
}