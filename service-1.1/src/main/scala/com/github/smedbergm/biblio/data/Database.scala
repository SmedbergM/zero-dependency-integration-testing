package com.github.smedbergm.biblio.data

import scala.io.{Codec, Source}
import scala.util.matching.Regex

import com.github.smedbergm.biblio.handlers.BadRequestException
import com.typesafe.scalalogging.LazyLogging

object Database extends LazyLogging {
  logger.debug("Initializing database")
  private val books: Map[String,Book] = Source.fromResource("books.csv")(Codec.UTF8).getLines().drop(1).map {
    line =>
      val title :: author :: isbn :: yearString :: Nil = line.split(",").toList
      isbn -> Book(title, author, isbn, Integer.parseInt(yearString))
  }.toMap
  books.foreach {
    case (_, book) => logger.debug(book.toString)
  }

  private val legalCharacters = """([\w\s]*)""".r

  def searchBooksBy(optQuery: Option[String], field: Book => String): Set[Book] = optQuery match {
    case None => books.values.toSet
    case Some(legalCharacters(query)) =>
      val regexes = query.split("""\s+""").toSet.map((word: String) => s"(?i:$word)".r)
      books.values.filter(book => regexes.forall(_.findFirstIn(field(book)).nonEmpty)).toSet
    case _ => throw BadRequestException(s"Query ${optQuery.getOrElse("")} is not alphanumeric.")
  }

  private val users: Set[User] = {
    Source.fromResource("users.csv")(Codec.UTF8).getLines().drop(1).map {
      line =>
        val userID :: username :: firstName :: lastName :: Nil = line.split(",").toList
        User(username, userID.toLong, firstName,lastName)
    }.toSet
  }
  users.foreach(user => logger.debug(user.toString))

  private val legalUsernameCharacters = """[\w.@]*""".r
  private def searchUsersBy(regexes: Iterable[Regex], field: User => String): Set[User] = {
    users.filter(user => regexes.forall(_.findFirstIn(field(user)).nonEmpty))
  }
  def searchUsers(query: String): Set[User] = query match {
    case legalUsernameCharacters() =>
      val regexes = query.split("""\s+""").map(q => s"(?i:$q)".r)
      searchUsersBy(regexes, _.firstName) ++ searchUsersBy(regexes, _.lastName) ++ searchUsersBy(regexes, _.username)
    case _ => throw BadRequestException("Unsupported characters in query")
  }

  private val checkedOutItems: Map[Long,Set[String]] = Source.fromResource("checkedOutItems.csv")
    .getLines().drop(1).foldLeft(Map.empty[Long,Set[String]]){
    case (m, line) =>
      val userString :: isbn :: Nil = line.split(",").toList
      val userID = userString.toLong
      m.get(userID) match {
        case None => m + (userID -> Set(isbn))
        case Some(isbns) => m + (userID -> (isbns + isbn))
      }
  }
  checkedOutItems.foreach {
    case (userID, isbns) => logger.debug(s"User $userID has $isbns checked out.")
  }

  def getCheckedOutItems(userID: Long): Set[Book] = for {
    isbns <- checkedOutItems.get(userID).toSet[Set[String]]
    isbn <- isbns
    book <- books.get(isbn).toSet[Book]
  } yield book
}

case class Book(title: String, author: String, isbn: String, year: Int)
case class User(username: String, userID: Long, firstName: String, lastName: String)

