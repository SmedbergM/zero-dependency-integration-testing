package com.github.smedbergm.biblio.handlers

import scalaz.concurrent.Task

import com.github.smedbergm.biblio.data.Database
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Request, Response}
import org.http4s.dsl._

object BookHandler extends Authentication with JsonSupport {
  def handle(request: Request,
             optTitle: Option[String],
             optAuthor: Option[String],
             optISBN: Option[String]): Task[Response] = {
    val innerTask = for {
      _ <- verify(request)
      hits = if (optTitle.isEmpty && optAuthor.isEmpty && optISBN.isEmpty) {
        throw BadRequestException("No query provided")
      } else {
        Database.searchBooksBy(optTitle, _.title) &
          Database.searchBooksBy(optAuthor, _.author) &
          Database.searchBooksBy(optISBN, _.isbn)
      }
      response <- Ok(write(hits))
    } yield response
    innerTask.handleWith(BiblioException.toErrorResponse)
  }
}
