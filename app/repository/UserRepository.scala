package repository

import anorm.{SQL, SqlParser}
import anorm.SqlParser.{scalar, str}
import play.api.db.DBApi
import com.github.t3hnar.bcrypt._

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class UserRepository @Inject()(override val dbAPI: DBApi) extends AbstractRepository {

 def createOfficer(username: Username, password: Password, fullName : String): Try[Option[String]] =
  db.withConnection{implicit connection =>
    SQL("INSERT INTO officer(username, password, full_name) VALUES ({username}, {password}, {full_name})").on(
             Symbol("username") -> username.value,
             Symbol("password") -> password.hash,
             Symbol("full_name") -> fullName
    ).executeInsert1("username")(scalar[String].singleOpt)
  }

  def deleteOfficer(username: Username): Int = db.withConnection{implicit connection =>
    SQL("DELETE FROM officer WHERE username={username}")
      .on(Symbol("username") -> username.value)
      .executeUpdate()
  }


  def getOfficerPasswordHash(username: String): Option[String] = db.withConnection{ implicit connection =>
      SQL("SELECT password FROM officer WHERE username={username}").on(
        Symbol("username") -> username,
      ).executeQuery().as[Option[String]](str(1).singleOpt)
    }


}


case class Username(value: String) extends AnyVal
case class Password(value: String) extends AnyVal{
  def hash: String = value.bcrypt
  def isMatched(hash: String): Boolean = value.isBcrypted(hash)
}
trait User{
  def username: Username
  def password: Password
}


case class Admin(username: Username, password: Password) extends User

