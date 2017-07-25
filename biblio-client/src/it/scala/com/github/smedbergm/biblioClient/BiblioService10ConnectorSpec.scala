package com.github.smedbergm.biblioClient

import scala.util.{Success, Failure}

import com.fasterxml.jackson.databind.JsonMappingException
import com.github.smedbergm.biblioClient.data.{User, Book}
import org.scalatest.FlatSpec

class BiblioService10ConnectorSpec extends FlatSpec {
  ignore should "try to return a list of Users" in {
    BiblioService10Connector.getUsers("edijkstra") match {
      case Success(users) => assert(users contains User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))
    }
    BiblioService10Connector.getUsers("odersky") match {
      case Success(Nil) => ()
    }
  }

  "searchBooks" should "try to return a list of Books" in {
    BiblioService10Connector.searchBooks(titleQuery = Some("programming")) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
        "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }

    BiblioService10Connector.searchBooks(titleQuery = Some("c++")) match {
      case Failure(_: JsonMappingException) => ()
    }

    BiblioService10Connector.searchBooks(authorQuery = Some("martin")) match {
      case Success(books) => assert(books.toSet[Book].map(_.author) === Set("Robert C. Martin", "Martin Odersky"))
    }
    BiblioService10Connector.searchBooks(authorQuery = Some("martin sky")) match {
      case Success(books) => assert(books.map(_.author) === List("Martin Odersky"))
    }
    BiblioService10Connector.searchBooks(authorQuery = Some("martin blue sky")) match {
      case Success(Nil) => ()
    }

    BiblioService10Connector.searchBooks(authorQuery = Some("Kernighan & Ritchie")) match {
      case Failure(_: JsonMappingException) => ()
    }
  }

  ignore should "get a user's checked out items" in {
    BiblioService10Connector.getCheckedOutItems(1) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }
    BiblioService10Connector.getCheckedOutItems(2) match {
      case Success(Nil) => ()
    }
  }
}
