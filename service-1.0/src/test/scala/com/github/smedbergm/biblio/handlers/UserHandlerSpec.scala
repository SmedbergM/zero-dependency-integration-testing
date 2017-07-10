package com.github.smedbergm.biblio.handlers

import scalaz.{\/-, -\/}

import com.github.smedbergm.biblio.exceptions.BadRequestException
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.Request
import org.json4s.{JValue, MappingException}
import org.scalatest.FlatSpec

class UserHandlerSpec extends FlatSpec with JsonSupport {
  "UserHandler.handle" should "return hits on first name, last name, and userID" in {
    val task = for {
      request <- Request().withBody(write(Map("query" -> "ac.uk")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 2)
    } yield users
    task.run

    val task2 = for {
      request <- Request().withBody(write(Map("query" -> "texas")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 1)
    } yield users
    task2.run

    val task3 = for {
      request <- Request().withBody(write(Map("query" -> "wisconsin")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.isEmpty)
    } yield users
    task3.run

    val task4 = for {
      request <- Request().withBody(write(Map("query" -> "al")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 2)
    } yield users
    task4.run

    val badRequest0 = for {
      request <- Request().withBody(write(Map("Query" -> "texas")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 1)
    } yield users
    badRequest0.attemptRun match {
      case -\/(_: MappingException) => ()
      case -\/(exc) => throw exc
      case \/-(v) => assert(false, s"I should not have succeeded, but I did with value $v")
    }

    val badRequest1 = for {
      request <- Request().withBody(write(Map("query" -> "north~dakota")))
      response <- UserHandler.handle(request)
      responseText <- response.as[String]
      users <- parse[List[JValue]](responseText)
      _ = assert(users.length === 1)
    } yield users
    badRequest1.attemptRun match {
      case -\/(_: BadRequestException) => ()
      case -\/(exc) => throw exc
      case \/-(v) => assert(false, s"I should not have succeeded, but I did with value $v")
    }
  }
}
