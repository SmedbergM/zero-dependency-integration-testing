package com.github.smedbergm.biblio

import com.github.smedbergm.biblio.handlers.{BookHandler, AuthHandler, UserHandler, UserItemsHandler}
import org.http4s.{MediaType, HttpService}
import org.http4s.dsl._

object BiblioService {
  import Queries._

  val service: HttpService = HttpService {
    case request @ GET -> Root => Ok(usage).withType(MediaType.`text/plain`)

    case request @ GET -> Root / "auth" :? Username(username) +& Password(password) => AuthHandler.handle(username,password)

    case request @ GET -> Root / "api" / "book" :? Title(optTitle) +& Author(optAuthor) +& ISBN(optIsbn) =>
      BookHandler.handle(request, optTitle, optAuthor, optIsbn)
    case request @ GET -> Root / "api" / "user" :? Search(query) => UserHandler.handle(request, query)
    case request @ GET -> Root / "api" / "user" / "items" :? UserID(userID) => UserItemsHandler.handle(request, userID)
  }

  private val usage =
    """Welcome to Matthew Smedberg's talk on Zero-Dependency Integration Testing!
      |
      |This HTTP service exposes a few resources that the client application will make use of:
      |
      |GET / => Returns this message
      |
      |GET /auth?username=...&password=... =>
      |  Returns an access token for further browsing
      |
      |Authenticated routes:
      |Every call to these routes must include a header
      |Key: Authorization
      |Value: Bearer {access-token}
      |
      |GET /api/book?[author=...][&title=...][&isbn=...] =>
      |  Reads the provided query parameters (must provide at least one) and returns books matching all.
      |GET /api/user?search=... =>
      |  Returns a list of users matching the provided search string (on username, first name, or last name)
      |GET /api/user/items?userID=... =>
      |  Returns a list of items checked out to the provided userID
    """.stripMargin
}

object Queries {
  object Username extends QueryParamDecoderMatcher[String]("username")
  object Password extends QueryParamDecoderMatcher[String]("password")

  object Title extends OptionalQueryParamDecoderMatcher[String]("title")
  object Author extends OptionalQueryParamDecoderMatcher[String]("author")
  object ISBN extends OptionalQueryParamDecoderMatcher[String]("isbn")

  object Search extends QueryParamDecoderMatcher[String]("search")

  object UserID extends QueryParamDecoderMatcher[Long]("userID")
}