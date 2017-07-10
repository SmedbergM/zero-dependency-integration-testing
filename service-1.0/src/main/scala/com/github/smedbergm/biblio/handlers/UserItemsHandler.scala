package com.github.smedbergm.biblio.handlers

import scala.io.Source
import scalaz.concurrent.Task

import com.github.smedbergm.biblio.json.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Request, Response}
import org.http4s.dsl._
import org.http4s.MediaType.{`application/json` => Json}

object UserItemsHandler extends JsonSupport with LazyLogging {
  def handle(request: Request): Task[Response] = for {
    body <- request.as[String]
    parsedBody <- parse[RequestBody](body)
    isbns = checkedOutItems.get(parsedBody.userID).toSet.flatten
    response <- Ok(write(isbns)).withType(Json)
  } yield response

  private case class RequestBody(userID: Long)

  private val checkedOutItems: Map[Long,Set[Long]] = Source.fromResource("checkedOutItems.csv")
    .getLines().drop(1).foldLeft(Map.empty[Long,Set[Long]]){
    case (m, line) =>
      val userID :: isbn :: Nil = line.split(",").toList.map(_.toLong)
      m.get(userID) match {
        case None => m + (userID -> Set(isbn))
        case Some(isbns) => m + (userID -> (isbns + isbn))
      }
  }
  logger.debug(s"Initializing checked out items database: $checkedOutItems")
}
