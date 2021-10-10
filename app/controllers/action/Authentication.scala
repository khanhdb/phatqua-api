package controllers.action

import play.api.Logger
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionBuilder, AnyContent, BodyParsers, Request, Result, WrappedRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Authenticated @Inject()(override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest,AnyContent]{

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username") match {
      case None =>
        Future.successful(Unauthorized)
      case Some(username) =>
        logger.debug(s"user $username passed authentication")
        block(UserRequest(username, request))
    }
  }
}

case class UserRequest[A](username: String, request: Request[A]) extends WrappedRequest[A](request)


@Singleton
class Admin @Inject()(override val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest,AnyContent] {

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username") match {
      case Some(username) if username == "admin" =>
        logger.debug(s"user $username passed admin authentication")
        block(UserRequest(username, request))
      case _ =>
        Future.successful(Unauthorized("you need admin permission"))
    }
  }
}


