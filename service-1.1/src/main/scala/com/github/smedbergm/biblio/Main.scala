package com.github.smedbergm.biblio

import scalaz.concurrent.Task

import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

object Main extends ServerApp {
  override def server(args: List[String]): Task[Server] = BlazeBuilder
    .bindHttp(8180)
    .mountService(BiblioService.service)
    .start
}

