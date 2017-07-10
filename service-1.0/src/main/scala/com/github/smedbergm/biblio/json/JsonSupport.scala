package com.github.smedbergm.biblio.json

import scalaz.concurrent.Task

import org.json4s.DefaultFormats

trait JsonSupport {
  implicit val formats: org.json4s.Formats = DefaultFormats

  def parse[T <: AnyRef](in: String)(implicit mnf: Manifest[T]): Task[T] = Task.delay {
    org.json4s.jackson.JsonMethods.parse(in).extract[T]
  }

  def write[T <: AnyRef](t: T): Task[String] = Task.delay {
    org.json4s.jackson.Serialization.write(t)
  }
}
