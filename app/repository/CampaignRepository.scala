package repository

import anorm.{Macro, SQL, SqlParser}
import controllers.CreateCampaign
import play.api.db.DBApi
import play.api.libs.json.{Json, OFormat}

import javax.inject.{Inject, Singleton}

@Singleton
class CampaignRepository @Inject()(override val dbAPI: DBApi) extends AbstractRepository {
  def create(campaign: CreateCampaign): Option[Int] = db.withConnection{ implicit connection =>
    SQL("INSERT INTO campaign(name, location) VALUES({name}, {location})").on(
        Symbol("name") -> campaign.name,
        Symbol("location") -> campaign.location
    ).executeInsert(SqlParser.scalar[Int].singleOpt)
  }

  def allCampagins: List[Campaign] = db.withConnection{ implicit connection =>
    val parser = Macro.namedParser[Campaign]
    SQL("SELECT * FROM campaign").executeQuery().as(parser.*)
  }

  def delete(id: Int): Int = db.withConnection{implicit connection =>
    SQL("DELETE FROM campaign WHERE id={id}").on(
      Symbol("id") -> id,
    ).executeUpdate()
  }

  case class Campaign(id: Int, name: String, location: String)

  object Campaign {
    implicit val fmt: OFormat[Campaign] = Json.format[Campaign]
  }
}

