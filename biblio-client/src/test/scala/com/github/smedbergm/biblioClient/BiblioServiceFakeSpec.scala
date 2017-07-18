package com.github.smedbergm.biblioClient

import com.github.smedbergm.biblioClient.data.{User, Book}
import com.fasterxml.jackson.databind.JsonMappingException
import scala.util.{Success, Failure}

import org.scalatest.FlatSpec

class BiblioServiceFakeSpec extends FlatSpec{
  "getUsers" should "try to return a list of Users" in {
    BiblioServiceFake.getUsers("edijkstra") match {
      case Success(users) => assert(users contains User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))
    }
    BiblioServiceFake.getUsers("odersky") match {
      case Success(Nil) => ()
    }
  }

  "searchBooks" should "try to return a list of Books" in {
    BiblioServiceFake.searchBooks(titleQuery = Some("programming")) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
        "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }

    BiblioServiceFake.searchBooks(titleQuery = Some("c++")) match {
      case Failure(_: JsonMappingException) => ()
    }

    BiblioServiceFake.searchBooks(authorQuery = Some("martin")) match {
      case Success(books) => assert(books.toSet[Book].map(_.author) === Set("Robert C. Martin", "Martin Odersky"))
    }
    BiblioServiceFake.searchBooks(authorQuery = Some("martin sky")) match {
      case Success(books) => assert(books.map(_.author) === List("Martin Odersky"))
    }
    BiblioServiceFake.searchBooks(authorQuery = Some("martin blue sky")) match {
      case Success(Nil) => ()
    }

    BiblioServiceFake.searchBooks(authorQuery = Some("Kernighan & Ritchie")) match {
      case Failure(_: JsonMappingException) => ()
    }
  }

  "" should "" in {
    BiblioServiceFake.getCheckedOutItems(1) match {
      case Success(books) => assert(books.toSet[Book].map(_.title) === Set(
        "Programming in Scala",
        "Functional Programming in Scala"
      ))
    }
    BiblioServiceFake.getCheckedOutItems(2) match {
      case Success(Nil) => ()
    }
  }

}
