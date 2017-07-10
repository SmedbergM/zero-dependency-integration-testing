package com.github.smedbergm.biblio.handlers

import scalaz.{\/-, -\/}

import com.github.smedbergm.biblio.exceptions.BadRequestException
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.Request
import org.json4s.JValue
import org.scalatest.FlatSpec

class BookHandlerSpec extends FlatSpec with JsonSupport {
  "The BookHandler" should "return all books if given an empty object" in {

    val emptyBodyTask = for {
      request <- Request().withBody(write(Map.empty))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books

    emptyBodyTask.map { books =>
      assert(books.length === 6)
    }.run
  }

  it should "return an array if given a substring of an author's name" in {

    val authorQueryTask = for {
      request <- Request().withBody(write(Map("author" -> "martin")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    authorQueryTask.map { books =>
      assert(books.length == 2)
    }.run

    val nonMatchingAuthorQueryTask = for {
      request <- Request().withBody(write(Map("author" -> "dijkstra")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    nonMatchingAuthorQueryTask.map { books =>
      assert(books.isEmpty)
    }.run
  }

  it should "only return books that match all words" in {
    val task = for {
      request <- Request().withBody(write(Map("author" -> "rob mart")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    task.map { books =>
      assert(books.length == 1)
    }.run
  }

  it should "return an array of matching titles" in {
    val task = for {
      request <- Request().withBody(write(Map("title" -> "scala")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    task.map { books =>
      assert(books.length === 2)
    }

    val nonMatchingTask = for {
      request <- Request().withBody(write(Map("title" -> "Javascript")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    nonMatchingTask.map { books =>
      assert(books.isEmpty)
    }

    val illegalCharacters = for {
      request <- Request().withBody(write(Map("title" -> "c++")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    illegalCharacters.attemptRun match {
      case -\/(exc: BadRequestException) => ()
      case -\/(exc) => throw exc
      case \/-(x) => assert(false, s"I should not have succeeded, but I got value $x")
    }
  }

  it should "return an array of ISBN matches" in {
    val matchingTask = for {
      request <- Request().withBody(write(Map("isbn" -> "88")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    matchingTask.map { books =>
      assert(books.length === 2)
    }.run

    val nonMatchingTask = for {
      request <- Request().withBody(write(Map("isbn" -> "883")))
      response <- BookHandler.handle(request)
      responseBody <- response.as[String]
      books <- parse[List[JValue]](responseBody)
    } yield books
    nonMatchingTask.map { books =>
      assert(books.isEmpty)
    }.run
  }

  it should "intersect the results of provided query parts" in {
    val titleOnly = Map("title" -> "scala")
    val authorOnly = Map("author" -> "martin")
    val task = for {
      titleRequest <- Request().withBody(write(titleOnly))
      authorRequest <- Request().withBody(write(authorOnly))
      jointRequest <- Request().withBody(write(titleOnly ++ authorOnly))
      titleResponse <- BookHandler.handle(titleRequest)
      authorResponse <- BookHandler.handle(authorRequest)
      jointResponse <- BookHandler.handle(jointRequest)
      titleResponseText <- titleResponse.as[String]
      authorResponseText <- authorResponse.as[String]
      jointResponseText <- jointResponse.as[String]
      titleHits <- parse[Set[JValue]](titleResponseText)
      authorHits <- parse[Set[JValue]](authorResponseText)
      jointHits <- parse[Set[JValue]](jointResponseText)
      _ = assert(jointHits === (titleHits & authorHits))
      _ = assert(jointHits.size === 1)
    } yield ()
    task.run
  }
}
