package com.github.smedbergm.biblio.handlers

import scalaz.concurrent.Task

import org.http4s.{Response, Challenge}
import org.http4s.dsl._

sealed trait BiblioException extends Throwable {
  val errorMessage: String
  override def getMessage: String = errorMessage
}

object BiblioException {
  val toErrorResponse: PartialFunction[Throwable, Task[Response]] = {
    case UnauthorizedException(errorMessage) => Unauthorized(Challenge("",""))
    case BadRequestException(errorMessage) => BadRequest(errorMessage)
    case exc => InternalServerError(exc.getMessage)
  }
}

case class UnauthorizedException(errorMessage: String) extends BiblioException
case class BadRequestException(errorMessage: String) extends BiblioException
