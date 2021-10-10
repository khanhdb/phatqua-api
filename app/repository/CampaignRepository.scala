package repository

import anorm.{SQL, SqlParser}
import controllers.Campaign
import play.api.db.DBApi

import javax.inject.{Inject, Singleton}

@Singleton
class CampaignRepository @Inject()(override val dbAPI: DBApi) extends AbstractRepository {
  def create(campaign: Campaign): Option[Int] = db.withConnection{implicit connection =>
    SQL("INSERT INTO campaign(name, location) VALUES({name}, {location})").on(
        Symbol("name") -> campaign.name,
        Symbol("location") -> campaign.location
    ).executeInsert(SqlParser.scalar[Int].singleOpt)
  }

  def delete(id: Int): Int = db.withConnection{implicit connection =>
    SQL("DELETE FROM campaign WHERE id={id}").on(
      Symbol("id") -> id,
    ).executeUpdate()
  }
}

