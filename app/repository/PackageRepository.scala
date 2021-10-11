package repository

import anorm.{RowParser, SQL, SqlParser, ~}
import controllers.CreatePackage
import play.api.Logger
import play.api.db.DBApi
import play.api.libs.json._
import repository.PackageStatus.{DONE, PackageStatus}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

@Singleton
class PackageRepository @Inject()(override val dbAPI: DBApi) extends AbstractRepository {
  private val logger = Logger(this.getClass)
  def create(pkg: CreatePackage): Future[Option[String]] = Future(db.withConnection{ implicit connection =>
    SQL("INSERT INTO package(name, phone, campaign_id) VALUES({name}, {phone}, {campaign_id})").on(
        Symbol("name") -> pkg.name,
        Symbol("phone") ->pkg.phone,
        Symbol("campaign_id") -> pkg.campaignId
    ).executeInsert(SqlParser.scalar[String].singleOpt)
  }).andThen{
    case Failure(exception) =>
      exception.printStackTrace()
      logger.debug(s"failed to add package : ${pkg.phone}")
  }


  def allPackages: List[Package] = db.withConnection(implicit connection =>
    SQL("SELECT package.name, phone, status, cam.name as campaign FROM package JOIN campaign cam on cam.id = package.campaign_id")
      .executeQuery().as(Package.parser.*)
  )

  def confirmReceivingPackage(phone: String, code: String, officer: String): Int = Try( db.withConnection{implicit connection =>
    SQL(s"UPDATE package set status={status}, updated_by={officer}, updated_at=NOW() WHERE phone={phone} AND verify_code={code} AND status <> ${DONE.id}")
      .on(
        Symbol("code") -> code,
        Symbol("status") -> PackageStatus.DONE.id,
        Symbol("phone") -> phone,
        Symbol("officer") -> officer,
      ).executeUpdate()
  }).getOrElse(0)

  def packagesByOfficer(officer: String): List[Package] = db.withConnection{implicit connection =>
    SQL("SELECT package.name, phone, status, cam.name as campaign FROM package JOIN campaign cam on cam.id = package.campaign_id " +
      "WHERE updated_by={officer}")
      .on(Symbol("officer") -> officer)
      .executeQuery().as(Package.parser.*)
  }

  def updateVerifyCode(newCode: String, phone: String, officer: String): Int = Try( db.withConnection{implicit connection =>
    SQL("UPDATE package set verify_code = {newCode}, updated_by={officer}, updated_at=NOW() WHERE phone={phone}")
      .on(
        Symbol("newCode") -> newCode,
        Symbol("phone") -> phone,
        Symbol("officer") -> officer,
      ).executeUpdate()
  }).getOrElse(0)
}

case class Package(name: String, phone: String, status: PackageStatus, campaign: String)

object Package{
  val parser: RowParser[Package] = {
      SqlParser.get[String]("name") ~
      SqlParser.str("phone") ~
      SqlParser.int("status") ~
      SqlParser.str("campaign") map {
        case name ~ phone ~ status ~ campaign =>
          Package(name, phone, PackageStatus(status), campaign)
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

