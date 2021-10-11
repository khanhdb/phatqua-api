package controllers.action

import play.api.Logger
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionBuilder, AnyContent, BodyParsers, Request, Result, WrappedRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait Authenticated extends ActionBuilder[UserRequest,AnyContent]{

  private val logger = Logger(this.getClass)

  def usernameCondition(username: String): Boolean

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username") match {
      case Some(username) if usernameCondition(username) =>
        logger.debug(s"user $username passed authentication")
        block(UserRequest(username, request))
      case _ =>
        Future.successful(Unauthorized)
    }
  }
}

case class UserRequest[A](username: String, request: Request[A]) extends WrappedRequest[A](request)


@Singleton
class AdminPermission @Inject()(override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends Authenticated{
  override def usernameCondition(username: String): Boolean = username == "admin"
}


@Singleton
class OfficerPermission @Inject()(override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends Authenticated{
  override def usernameCondition(username: String): Boolean = username != "admin"
}

@Singleton
class LoggedIn @Inject()(override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends Authenticated{
  override def usernameCondition(username: String): Boolean = true
}
