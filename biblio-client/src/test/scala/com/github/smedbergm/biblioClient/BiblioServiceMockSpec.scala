package com.github.smedbergm.biblioClient

import com.github.smedbergm.biblioClient.data.{User, Book}
import com.fasterxml.jackson.databind.JsonMappingException
import scala.util.{Success, Failure}

import org.scalatest.FlatSpec

class BiblioServiceMockSpec extends FlatSpec{
  "getUsers" should "try to return a list of Users" in {
    BiblioServiceMock.getUsers("edijkstra") match {
      case Success(users) => assert(users contains User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))
    }
    BiblioServiceMock.getUsers("odersky") match {
      case Success(Nil) => ()
    }
  }

  "searchBooks" should "try to return a list of Books" in {
    BiblioServiceMock.searchBooks(titleQuery = Some("programming")) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
        "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }

    BiblioServiceMock.searchBooks(titleQuery = Some("c++")) match {
      case Failure(_: JsonMappingException) => ()
    }

    BiblioServiceMock.searchBooks(authorQuery = Some("martin")) match {
      case Success(books) => assert(books.toSet[Book].map(_.author) === Set("Robert C. Martin", "Martin Odersky"))
    }
    BiblioServiceMock.searchBooks(authorQuery = Some("martin sky")) match {
      case Success(books) => assert(books.map(_.author) === List("Martin Odersky"))
    }
    BiblioServiceMock.searchBooks(authorQuery = Some("martin blue sky")) match {
      case Success(Nil) => ()
    }

    BiblioServiceMock.searchBooks(authorQuery = Some("Kernighan & Ritchie")) match {
      case Failure(_: JsonMappingException) => ()
    }
  }

  "" should "" in {
    BiblioServiceMock.getCheckedOutItems(1) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }
    BiblioServiceMock.getCheckedOutItems(2) match {
      case Success(Nil) => ()
    }
  }

}
