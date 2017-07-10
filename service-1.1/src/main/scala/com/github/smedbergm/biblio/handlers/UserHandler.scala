package com.github.smedbergm.biblio.handlers

import scalaz.concurrent.Task

import com.github.smedbergm.biblio.data.Database
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Request, Response}
import org.http4s.dsl._

object UserHandler extends Authentication with JsonSupport {
  def handle(request: Request, query: String): Task[Response] = {
    val innerTask = for {
      _ <- verify(request)
      users = Database.searchUsers(query)
      response <- Ok(write(users))
    } yield response
    innerTask.handleWith(BiblioException.toErrorResponse)
  }
}
