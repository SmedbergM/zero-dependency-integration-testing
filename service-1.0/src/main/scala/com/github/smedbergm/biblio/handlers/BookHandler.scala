package com.github.smedbergm.biblio.handlers

import scala.io.{Codec, Source}
import scalaz.concurrent.Task

import com.github.smedbergm.biblio.exceptions.BadRequestException
import org.http4s.dsl._
import com.github.smedbergm.biblio.json.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{MediaType, Request, Response}

object BookHandler extends JsonSupport with LazyLogging {
  def handle(request: Request): Task[Response] = {
    for {
      body <- request.as[String]
      parsedBody <- parse[RequestBody](body)
      results = search(parsedBody.title, book => book.title) &
        search(parsedBody.author, book => book.author) &
        search(parsedBody.isbn, book => book.isbn)
      response <- Ok(write(results)).withType(MediaType.`application/json`)
    } yield response
  }

  private val legalCharacters = """[\w\s]*""".r

  private def search(optQuery: Option[String], field: Book => String): Set[Book] = optQuery match {
    case None => books // If the request doesn't specify a query for a field, everything matches
    case Some(query) => query match {
      case legalCharacters() =>
        val regexes = query.split("""\s+""").toSet.map((word: String) => s"(?i:$word)".r)
        books.filter(book => regexes.forall(_.findFirstIn(field(book)).nonEmpty))
      case _ => throw BadRequestException("Queries must contain only alphanumeric characters and spaces")
    }
  }

  private case class RequestBody(title: Option[String], author: Option[String], isbn: Option[String])

  private val books: Set[Book] = Source.fromResource("books.csv")(Codec.UTF8).getLines().drop(1).map {
    line =>
      val title :: author :: isbn :: yearString :: Nil = line.split(",").toList
      Book(title, author, isbn, Integer.parseInt(yearString))
  }.toSet
  logger.debug(s"Initializing books database: $books")
}

case class Book(
  title: String,
  author: String, // limitation: some books have multiple authors!
  isbn: String,
  year: Int
)