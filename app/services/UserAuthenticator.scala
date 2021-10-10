package services

import com.typesafe.config.ConfigFactory
import repository.{Password, UserRepository}

import javax.inject.Inject

trait UserAuthenticator {
  def validate(username: String, password: String): Boolean
}


class DefaultUserAuthenticator @Inject()(userRepository: UserRepository) extends UserAuthenticator {
  override def validate(username: String, password: String): Boolean = username match {
    case "admin" =>
      val adminPasswd = ConfigFactory.load().getString("admin.password")
      password == adminPasswd
    case _ =>
      userRepository.getOfficerPasswordHash(username) match {
        case Some(hash) =>
          Password(password).isMatched(hash)
        case None =>
          false
      }
  }
}