package com.github.smedbergm.biblio.handlers

import scala.io.{Codec, Source}
import scalaz.concurrent.Task

import com.github.smedbergm.biblio.exceptions.BadRequestException
import com.github.smedbergm.biblio.json.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Request, Response}
import org.http4s.dsl._
import org.http4s.MediaType.{`application/json` => Json}

object UserHandler extends JsonSupport with LazyLogging {

  def handle(request: Request): Task[Response] = for {
    requestBody <- request.as[String]
    parsedBody <- parse[RequestBody](requestBody)
    hits = search(parsedBody.query, _.username) ++
      search(parsedBody.query, _.firstName) ++
      search(parsedBody.query, _.lastName)
    response <- Ok(write(hits)).withType(Json)
  } yield response

  private val legalCharacters = """[\w\s.@]*""".r
  private def search(query: String, field: User => String): Set[User] = query match {
    case legalCharacters() =>
      val regexes = query.split("""\s+""").toSet[String].map(word => s"(?i:$word)".r)
      users.filter(user => regexes.forall(_.findFirstIn(field(user)).nonEmpty))
    case _ => throw BadRequestException("Searches must contain only letters, numbers, period and '@'.")
  }

  private case class RequestBody(query: String)
  private case class User(username: String, userID: Long, firstName: String, lastName: String)
  private val users: Set[User] = {
    Source.fromResource("users.csv")(Codec.UTF8).getLines().drop(1).map {
      line =>
        val userID :: username :: firstName :: lastName :: Nil = line.split(",").toList
        User(username, userID.toLong, firstName,lastName)
    }.toSet
  }
  logger.debug(s"Initializing user database: $users")

}

