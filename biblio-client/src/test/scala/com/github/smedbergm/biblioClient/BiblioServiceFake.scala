package com.github.smedbergm.biblioClient
import scala.io.{Codec, Source}
import scala.util.{Failure, Try, Success}
import scala.util.matching.Regex

import com.fasterxml.jackson.databind.JsonMappingException
import com.github.smedbergm.biblioClient.data.{User, Book}

object BiblioServiceFake extends BiblioServiceInterface {
  def regexify(query: String): Seq[Regex] = {
    query.split("""\s+""").toSeq.map(token => s"(?i:$token)".r)
  }

  override def getUsers(username: String): Try[List[User]] = {
    val legalCharacters = """[\w\s.@]*""".r
    username match {
      case legalCharacters() => Try{
        users.filter(user => regexify(username).forall(_.findFirstIn(user.username).nonEmpty)).toList
      }
      case _ => Failure(new JsonMappingException(""))
    }
  }

  override def searchBooks(titleQuery: Option[String], authorQuery: Option[String], isbnQuery: Option[String]): Try[List[Book]] = {
    val legalCharacters = """([\w\s]*)""".r
    def maybeTitleHits: Try[Set[Book]] = titleQuery match {
      case None => Success(books)
      case Some(legalCharacters(query)) => Try {
        books.filter(book => regexify(query).forall(_.findFirstIn(book.title).nonEmpty))
      }
      case _ => Failure(new JsonMappingException(""))
    }
    def maybeAuthorHits: Try[Set[Book]] = authorQuery match {
      case None => Success(books)
      case Some(legalCharacters(query)) => Try {
        books.filter(book => regexify(query).forall(_.findFirstIn(book.author).nonEmpty))
      }
      case _ => Failure(new JsonMappingException(""))
    }
    def maybeIsbnHits: Try[Set[Book]] = isbnQuery match {
      case None => Success(books)
      case Some(legalCharacters(query)) => Try {
        books.filter(book => regexify(query).forall(_.findFirstIn(book.isbn.toString).nonEmpty))
      }
      case _ => Failure(new JsonMappingException(""))
    }
    for {
      titleHits <- maybeTitleHits
      authorHits <- maybeAuthorHits
      isbnHits <- maybeIsbnHits
    } yield (titleHits & authorHits & isbnHits).toList
  }

  override def getCheckedOutItems(userID: Long): Try[List[Book]] = Try {
    checkedOutItems.getOrElse(userID, List.empty).flatMap{
      isbn => books.filter(book => book.isbn == isbn)
    }.toList
  }

  val books: Set[Book] = Source.fromResource("books.csv")(Codec.UTF8).getLines().drop(1).map {
    line =>
      val title :: author :: isbn :: yearString :: Nil = line.split(",").toList
      Book(isbn.toLong, title, author)
  }.toSet

  val users: Set[User] = {
    Source.fromResource("users.csv")(Codec.UTF8).getLines().drop(1).map {
      line =>
        val userID :: username :: firstName :: lastName :: Nil = line.split(",").toList
        User(userID.toLong, username, firstName,lastName)
    }.toSet
  }

  val checkedOutItems: Map[Long,Set[Long]] = Source.fromResource("checkedOutItems.csv")(Codec.UTF8)
    .getLines().drop(1).foldLeft(Map.empty[Long,Set[Long]]){
    case (m, line) =>
      val userID :: isbn :: Nil = line.split(",").toList.map(_.toLong)
      m.get(userID) match {
        case None => m + (userID -> Set(isbn))
        case Some(isbns) => m + (userID -> (isbns + isbn))
      }
  }

}
