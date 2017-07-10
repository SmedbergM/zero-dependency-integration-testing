package com.github.smedbergm.biblio.handlers

import scalaz.concurrent.Task

import com.github.smedbergm.biblio.data.Database
import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.{Request, Response}
import org.http4s.dsl._

object UserItemsHandler extends Authentication with JsonSupport {
  def handle(request: Request, userID: Long): Task[Response] = {
    val innerTask = for {
      _ <- verify(request)
      items = Database.getCheckedOutItems(userID)
      response <- Ok(write(items))
    } yield response
    innerTask.handleWith(BiblioException.toErrorResponse)
  }
}
