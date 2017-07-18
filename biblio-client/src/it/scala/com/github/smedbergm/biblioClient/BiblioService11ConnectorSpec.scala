package com.github.smedbergm.biblioClient

import scala.util.{Success, Failure}

import com.fasterxml.jackson.core.JsonParseException
import com.github.smedbergm.biblioClient.data.{User, Book}
import org.scalatest.{FlatSpec, BeforeAndAfter}

class BiblioService11ConnectorSpec extends FlatSpec with BeforeAndAfter {
  before {
    BiblioService11Connector.logout()
  }

  "getUsers" should "try to return a list of Users" in {
    BiblioService11Connector.getUsers("edijkstra") match {
      case Failure(_: IllegalStateException) => ()
    }

    assert(BiblioService11Connector.authenticate("berg", "awesomePassword!").isSuccess)

    BiblioService11Connector.getUsers("edijkstra") match {
      case Success(users) => assert(users contains User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))
    }
    BiblioService11Connector.getUsers("odersky") match {
      case Success(Nil) => ()
    }
  }

  "searchBooks" should "try to return a list of Books" in {
    BiblioService11Connector.searchBooks(titleQuery = Some("programming")) match {
      case Failure(_: IllegalStateException) => ()
    }

    assert(BiblioService11Connector.authenticate("berg", "awesomePassword!").isSuccess)

    BiblioService11Connector.searchBooks(titleQuery = Some("programming")) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
        "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }

    BiblioService11Connector.searchBooks(titleQuery = Some("c++")) match {
      case Failure(_: JsonParseException) => ()
    }

    BiblioService11Connector.searchBooks(authorQuery = Some("martin")) match {
      case Success(books) => assert(books.toSet[Book].map(_.author) === Set("Robert C. Martin", "Martin Odersky"))
    }
    BiblioService11Connector.searchBooks(authorQuery = Some("martin sky")) match {
      case Success(books) => assert(books.map(_.author) === List("Martin Odersky"))
    }
    BiblioService11Connector.searchBooks(authorQuery = Some("martin blue sky")) match {
      case Success(Nil) => ()
    }

    BiblioService11Connector.searchBooks(authorQuery = Some("Kernighan & Ritchie")) match {
      case Failure(_: JsonParseException) => ()
    }
  }

  "getCheckedOutItems" should "get a user id's checked out books" in {
    BiblioService11Connector.getCheckedOutItems(1) match {
      case Failure(_: IllegalStateException) => ()
    }

    assert(BiblioService11Connector.authenticate("berg", "awesomePassword!").isSuccess)

    BiblioService11Connector.getCheckedOutItems(1) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }
    BiblioService11Connector.getCheckedOutItems(2) match {
      case Success(Nil) => ()
    }
  }

}
