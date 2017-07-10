package com.github.smedbergm.biblio.handlers


import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Request, Header, Status}
import org.json4s.JValue
import org.scalatest.{FlatSpec, BeforeAndAfter}

class UserHandlerSpec extends FlatSpec with BeforeAndAfter with JsonSupport {

  before {
    AuthCache.accessTokens.clear()
  }

  "UserHandler.handle" should "return hits on first name, last name, and userID" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run
    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val request = Request().putHeaders(authorizationHeader)

    val noAuthTask = for {
      response <- UserHandler.handle(Request(), "ac.uk")
      _ = assert(response.status === Status.Unauthorized)
    } yield ()

    val task = for {
      response <- UserHandler.handle(request, "ac.uk")
      _ = assert(response.status === Status.Ok)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 2)
    } yield users
    task.run

    val task2 = for {
      response <- UserHandler.handle(request, "texas")
      _ = assert(response.status === Status.Ok)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 1)
    } yield users
    task2.run

    val task3 = for {
      response <- UserHandler.handle(request, "wisconsin")
      _ = assert(response.status === Status.Ok)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.isEmpty)
    } yield users
    task3.run

    val task4 = for {
      response <- UserHandler.handle(request, "al")
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 2)
    } yield users
    task4.run

    val badRequest1 = for {
      response <- UserHandler.handle(request, "north~dakota")
      _ = assert(response.status === Status.BadRequest)
    } yield ()
    badRequest1.run
  }
}
