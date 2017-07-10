package com.github.smedbergm.biblio

import com.github.smedbergm.biblio.handlers.{BookHandler, UserHandler, UserItemsHandler}
import org.http4s.{MediaType, HttpService}
import org.http4s.dsl._

object BiblioService {
  val service: HttpService = HttpService {
    case request @ GET -> Root => Ok(usage).withType(MediaType.`text/plain`)
    case request @ POST -> Root / "book" => BookHandler.handle(request)
    case request @ POST -> Root / "user" => UserHandler.handle(request)
    case request @ POST -> Root / "user" / "items" => UserItemsHandler.handle(request)
  }

  private val usage =
    """Welcome to Matthew Smedberg's talk on Zero-Dependency Integration Testing!
      |
      |This HTTP service exposes a few resources that the client application will make use of:
      |
      |GET / => Returns this message
      |POST /book => Reads a JSON query from the request body and returns a JSON array of matching books.
      |POST /user => Reads a JSON query from the request body and returns a JSON array of matching users.
      |POST /user/items => Reads a JSON query from the request body and returns a JSON array of checked out items.
    """.stripMargin
}
