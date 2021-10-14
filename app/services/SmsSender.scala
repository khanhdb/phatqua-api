package services

import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.Future


class SmsSender @Inject() (wsClient: WSClient){
  private val logger = Logger(this.getClass)

  def send(phone: String, content: String): Future[Int] = {
    logger.debug(s"sent : $content to number ${phone} ")
    val url = "https://chanphom.com/api-c3/user/fix-mission/send-sms"
    wsClient.url(url).post(Json.obj(
      content -> content,
      phone -> phone
    ))
    Future.successful(0)
  }
}
