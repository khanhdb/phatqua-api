package repository

import anorm.{RowParser, SQL, SqlParser, ~}
import controllers.CreatePackage
import play.api.Logger
import play.api.db.DBApi
import play.api.libs.json._
import repository.PackageStatus.{DONE, NEW, PLANNED, PackageStatus}

import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

@Singleton
class PackageRepository @Inject()(override val dbAPI: DBApi) extends AbstractRepository {
  private val logger = Logger(this.getClass)
  def create(pkg: CreatePackage): Future[Option[String]] = Future(db.withConnection{ implicit connection =>
    SQL("INSERT INTO package(name, phone, campaign_id, verify_code, address) VALUES({name}, {phone}, {campaign_id}, {verify_code}, {address})").on(
        Symbol("name") -> pkg.name,
        Symbol("phone") ->pkg.phone,
        Symbol("campaign_id") -> pkg.campaignId,
        Symbol("address") -> pkg.address,
        Symbol("verify_code") -> ""
    ).executeInsert(SqlParser.scalar[String].singleOpt)
  }).andThen{
    case Failure(exception) =>
      exception.printStackTrace()
      logger.debug(s"failed to add package : ${pkg.phone}")
  }


  def allPackages(campaignId: Int): List[Package] = db.withConnection(implicit connection =>
    SQL("SELECT package.name, phone, address, status, cam.name as campaign, package.updated_at FROM package JOIN campaign cam on cam.id = package.campaign_id WHERE package.campaign_id = {campaignId}")
      .on(
        Symbol("campaignId") -> campaignId
      )
      .executeQuery().as(Package.parser.*)
  )

  def packagesByStatus(campaignId: Int, status: PackageStatus): List[Package] = db.withConnection(implicit connection =>
    SQL(s"SELECT package.name, phone,address, status, cam.name as campaign, package.updated_at FROM package JOIN campaign cam on cam.id = package.campaign_id WHERE status={status} AND campaign_id={campaignId}")
      .on(
        Symbol("status") -> status.id,
        Symbol("campaignId") -> campaignId
      )
      .executeQuery().as(Package.parser.*)
  )

  def confirmReceivingPackage(phone: String, code: String, officer: String): Int = Try( db.withConnection{implicit connection =>
    SQL(s"UPDATE package set status={status}, updated_by={officer}, updated_at=NOW() WHERE phone={phone} AND verify_code={code} AND status <> ${DONE.id}")
      .on(
        Symbol("code") -> code,
        Symbol("status") -> DONE.id,
        Symbol("phone") -> phone,
        Symbol("officer") -> officer,
      ).executeUpdate()
  }).getOrElse(0)

  def packagesByOfficer(campaignId: Int, officer: String, status: Option[PackageStatus]): List[Package] = db.withConnection{ implicit connection =>
    status match {
      case None =>
        SQL("SELECT package.name, phone,address, status, cam.name as campaign, package.updated_at FROM package JOIN campaign cam on cam.id = package.campaign_id " +
          s"WHERE updated_by={officer} AND campaign_id={campaignId}")
          .on(
            Symbol("officer") -> officer,
            Symbol("campaignId") -> campaignId
          )
          .executeQuery().as(Package.parser.*)

      case Some(stt) =>
        SQL("SELECT package.name, phone,address, status, cam.name as campaign, package.updated_at FROM package JOIN campaign cam on cam.id = package.campaign_id " +
          s"WHERE updated_by={officer} AND status={status} AND campaign_id={campaignId}")
          .on(
            Symbol("officer") -> officer,
            Symbol("campaignId") -> campaignId,
            Symbol("status") -> stt.id
          )
          .executeQuery().as(Package.parser.*)
    }
  }

  def packageByVerifyCode(code: String): Option[Package] = db.withConnection{implicit connection =>
    SQL("SELECT package.name, phone, address, status, cam.name as campaign, package.updated_at FROM package JOIN campaign cam on cam.id = package.campaign_id " +
      "WHERE verify_code={code}")
      .on(Symbol("code") -> code)
      .executeQuery().as(Package.parser.singleOpt)
  }

  def initVerifyCode(newCode: String, phone: String, officer: String, note: String): Int = Try( db.withConnection{implicit connection =>
    SQL(s"UPDATE package set verify_code={newCode},note={note},updated_by={officer},updated_at=NOW(), status={status} WHERE phone={phone} AND status <> ${PLANNED.id}")
      .on(
        Symbol("newCode") -> newCode,
        Symbol("phone") -> phone,
        Symbol("officer") -> officer,
        Symbol("note") -> note,
        Symbol("status") -> PLANNED.id,
      ).executeUpdate()
  }).getOrElse(0)

  def updateVerifyCode(newCode: String, phone: String, officer: String): Int = Try( db.withConnection{implicit connection =>
    SQL("UPDATE package set verify_code = {newCode}, updated_by={officer}, updated_at=NOW() WHERE phone={phone}")
      .on(
        Symbol("newCode") -> newCode,
        Symbol("phone") -> phone,
        Symbol("officer") -> officer,
      ).executeUpdate()
  }).getOrElse(0)

  def verifyCodes: List[String] = db.withConnection{implicit connection =>
    SQL("SELECT verify_code FROM package").executeQuery().as(SqlParser.str(1).*)
  }
}

case class Package(name: String, phone: String, address: String, status: PackageStatus, campaign: String, updatedAt: Option[Date])

object Package{
  val parser: RowParser[Package] = {
      SqlParser.get[String]("name") ~
      SqlParser.str("phone") ~
      SqlParser.str("address") ~
      SqlParser.int("status") ~
      SqlParser.str("campaign") ~
      SqlParser.get[Option[Date]]("updated_at") map {
        case name ~ phone ~ address ~ status ~ campaign ~ updatedDate =>
          Package(name, phone, address, PackageStatus(status), campaign, updatedDate)
    }
  }

  implicit val fmt: Writes[Package] = Json.format[Package]
}

object PackageStatus extends Enumeration {
  type PackageStatus = Value
  val NEW : PackageStatus = Value
  val PLANNED : PackageStatus = Value
  val DONE : PackageStatus = Value
  implicit val fmt : Format[PackageStatus] = Format[PackageStatus](
    Reads(id => id.validate[Int].map(PackageStatus(_))),
    Writes(status => JsNumber(status.id))
  )
}

