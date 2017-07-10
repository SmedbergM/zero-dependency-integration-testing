package com.github.smedbergm.biblio.handlers

import scalaz.{\/-, -\/}

import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Request, Header, Status}
import org.json4s.{JValue, MappingException}
import org.scalatest.{FlatSpec, BeforeAndAfter}

class UserItemsHandlerSpec extends FlatSpec with BeforeAndAfter with JsonSupport {
  before {
    AuthCache.accessTokens.clear()
  }

  "UserItemsHandler.handle" should "read a userID and return a list of checked out books" in {
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
      response <- UserItemsHandler.handle(Request(),1)
      _ = assert(response.status === Status.Unauthorized)
    } yield ()
    noAuthTask.run

    val task0 = for {
      response <- UserItemsHandler.handle(request, 1)
      _ = assert(response.status === Status.Ok)
      responseText <- response.as[String]
      books <- parse[Set[JValue]](responseText)
      isbns = books.map(book => (book \ "isbn").extract[String])
      _ = assert(isbns === Set("0981531687","1617290653"))
    } yield isbns
    task0.run

    val task1 = for {
      response <- UserItemsHandler.handle(request, 4)
      _ = assert(response.status === Status.Ok)
      responseText <- response.as[String]
      books <- parse[Set[JValue]](responseText)
      _ = assert(books.isEmpty)
    } yield books
    task1.run
  }
}
