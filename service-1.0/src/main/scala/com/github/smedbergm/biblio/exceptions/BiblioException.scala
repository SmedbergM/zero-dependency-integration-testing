package com.github.smedbergm.biblio.exceptions

sealed trait BiblioException extends Throwable {
  val errorMessage: String
  override def getMessage: String = errorMessage
}
case class BadRequestException(errorMessage: String) extends BiblioException
