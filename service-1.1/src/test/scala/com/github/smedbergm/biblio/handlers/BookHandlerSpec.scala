package com.github.smedbergm.biblio.handlers

import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Status, Request, Header}
import org.json4s.JValue
import org.scalatest.{FlatSpec, BeforeAndAfter}

class BookHandlerSpec extends FlatSpec with BeforeAndAfter with JsonSupport {

  before {
    AuthCache.accessTokens.clear()
  }

  "BookHandler.handle" should "return 400 Bad Request if given an empty object" in {
    val emptyTaskNoAuth = for {
      response <- BookHandler.handle(Request(), None, None, None)
      _ = assert(response.status === Status.Unauthorized)
    } yield ()
    emptyTaskNoAuth.run

    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run

    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val emptyTask = for {
      response <- BookHandler.handle(Request().putHeaders(authorizationHeader), None, None, None)
      _ = assert(response.status === Status.BadRequest)
    } yield ()
    emptyTask.run
  }

  it should "return an array if given a substring of an author's name" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run

    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val request = Request().putHeaders(authorizationHeader)

    val (optTitle, optAuthor, optISBN) = (None, Some("martin"), None)
    val noAuthTask = for {
      response <- BookHandler.handle(Request(), optTitle, optAuthor, optISBN)
      _ = assert(response.status === Status.Unauthorized)
    } yield ()
    noAuthTask.run

    val authorQueryTask = for {
      response <- BookHandler.handle(request,optTitle, optAuthor, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    authorQueryTask.map { books =>
      assert(books.length == 2)
    }.run

    val optAuthor2 = Some("dijsktra")
    val nonMatchingAuthorQueryTask = for {
      response <- BookHandler.handle(request, optTitle, optAuthor2, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    nonMatchingAuthorQueryTask.map { books =>
      assert(books.isEmpty)
    }.run
  }

  it should "only return books that match all words" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run

    val (optTitle, optAuthor, optISBN) = (None, Some("rob mart"), None)
    val task = for {
      response <- BookHandler.handle(Request().putHeaders(Header("Authorization", s"Bearer $accessToken")), optTitle, optAuthor, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
      _ = assert(books.length === 1)
    } yield books
    task.run
  }

  it should "return an array of matching titles" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run
    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val request = Request().putHeaders(authorizationHeader)

    val (optTitle, optAuthor, optISBN) = (Some("scala"), None, None)

    val task = for {
      response <- BookHandler.handle(request, optTitle, optAuthor, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
      _ = assert(books.length === 2)
    } yield books
    task.run

    val optTitle2 = Some("javascript")
    val nonMatchingTask = for {
      response <- BookHandler.handle(request, optTitle2, optAuthor, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
      _ = assert(books.isEmpty)
    } yield books
    nonMatchingTask.run

    val optTitle3 = Some("c++")
    val illegalCharacters = for {
      response <- BookHandler.handle(request, optTitle3, optAuthor, optISBN)
      _ = assert(response.status === Status.BadRequest)
    } yield ()
    illegalCharacters.run
  }

  it should "return an array of ISBN matches" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run
    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val request = Request().putHeaders(authorizationHeader)

    val (optTitle, optAuthor, optISBN) = (None, None, Some("88"))
    val matchingTask = for {
      response <- BookHandler.handle(request, optTitle, optAuthor, optISBN)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
      _ = assert(books.length === 2)
    } yield books
    matchingTask.run

    val optISBN1 = Some("883")
    val nonMatchingTask = for {
      response <- BookHandler.handle(request, optTitle, optAuthor, optISBN1)
      _ = assert(response.status === Status.Ok)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
      _ = assert(books.isEmpty)
    } yield books
    nonMatchingTask.run
  }

  it should "intersect the results of provided query parts" in {
    val accessToken = AuthHandler.handle("count.rudin@florin.gov","sixthfinger").flatMap{
      response => response.as[String]
    }.flatMap {
      responseText => parse[JValue](responseText)
    }.map{
      jValue => (jValue \ "accessToken").extract[String]
    }.run
    val authorizationHeader = Header("Authorization", s"Bearer $accessToken")
    val request = Request().putHeaders(authorizationHeader)

    val (optTitle, optAuthor, optISBN) = (Some("scala"), Some("martin"), None)

    val task = for {
      titleResponse <- BookHandler.handle(request, optTitle, None, optISBN)
      authorResponse <- BookHandler.handle(request, None, optAuthor, optISBN)
      jointResponse <- BookHandler.handle(request, optTitle, optAuthor, optISBN)
      _ = assert(jointResponse.status === Status.Ok)
      titleResponseText <- titleResponse.as[String]
      authorResponseText <- authorResponse.as[String]
      jointResponseText <- jointResponse.as[String]
      titleHits <- parse[Set[JValue]](titleResponseText)
      authorHits <- parse[Set[JValue]](authorResponseText)
      jointHits <- parse[Set[JValue]](jointResponseText)
      _ = assert(titleHits.size === 2)
      _ = assert(authorHits.size === 2)
      _ = assert(jointHits === (titleHits & authorHits))
      _ = assert(jointHits.size === 1)
    } yield ()
    task.run
  }
}
