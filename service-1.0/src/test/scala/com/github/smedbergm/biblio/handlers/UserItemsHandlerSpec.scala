package com.github.smedbergm.biblio.handlers

import scalaz.{\/-, -\/}

import com.github.smedbergm.biblio.exceptions.BadRequestException
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.Request
import org.json4s.MappingException
import org.scalatest.FlatSpec

class UserItemsHandlerSpec extends FlatSpec with JsonSupport {
  "UserItemsHandler.handle" should "read a userID and return a list of checked out ISBNs" in {
    val task0 = for {
      request <- Request().withBody(write(Map("userID" -> 1)))
      response <- UserItemsHandler.handle(request)
      responseText <- response.as[String]
      isbns <- parse[Set[Long]](responseText)
      _ = assert(isbns === Set(981531687,1617290653))
    } yield isbns
    task0.run

    val task1 = for {
      request <- Request().withBody(write(Map("userID" -> 4)))
      response <- UserItemsHandler.handle(request)
      responseText <- response.as[String]
      isbns <- parse[Set[Long]](responseText)
      _ = assert(isbns.isEmpty)
    } yield isbns
    task1.run

    val task2 = for {
      request <- Request().withBody(write(Map("userid" -> 1)))
      response <- UserItemsHandler.handle(request)
      responseText <- response.as[String]
      isbns <- parse[Set[Long]](responseText)
      _ = assert(isbns.isEmpty)
    } yield isbns
    task2.attemptRun match {
      case -\/(_: MappingException) => ()
      case -\/(exc) => throw exc
      case \/-(v) => assert(false, s"I should have failed, but succeeded with value $v")
    }
  }
}
