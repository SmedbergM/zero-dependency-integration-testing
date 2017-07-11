package com.github.smedbergm.biblioClient

import com.fasterxml.jackson.databind.JsonMappingException
import com.github.smedbergm.biblioClient.data.User
import org.scalatest.FlatSpec

class BiblioClientSpec extends FlatSpec {
  val client = new BiblioClient

  "getUser" should "get a user" in {
    val client = new BiblioClient
    assert(client.getUser("edijkstra") === User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))

    try {
      client.getUser("odersky")
      assert(false, "I should have failed")
    } catch {
      case _: NoSuchElementException => ()
    }
  }

  "searchTitles" should "return a list of titles" in {
    assert(client.searchTitles("programming").toSet === Set(
      "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
      "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
      "Programming in Scala",
      "Functional Programming in Scala"
    ))

    try {
      client.searchTitles("c++")
      assert(false, "I should have failed!")
    } catch {
      case _: JsonMappingException => ()
    }
  }

  "searchAuthors" should "return a list of authors" in {
    assert(client.searchAuthors("martin").toSet === Set("Robert C. Martin", "Martin Odersky"))
    assert(client.searchAuthors("martin sky") === List("Martin Odersky"))
    assert(client.searchAuthors("martin blue sky").isEmpty)

    try {
      client.searchAuthors("Kernighan & Ritchie")
      assert(false, "I should have failed")
    } catch {
      case _: JsonMappingException => ()
    }
  }

  "searchByAuthor" should "return a list of titles" in {
    assert(client.searchByAuthor("martin").toSet === Set(
      "Clean Code: A Handbook of Agile Software Craftsmanship",
      "Programming in Scala"
    ))
    assert(client.searchByAuthor("bert martin") === List(
      "Clean Code: A Handbook of Agile Software Craftsmanship"
    ))
    assert(client.searchByAuthor("kernighan").isEmpty)
    try {
      client.searchByAuthor("kernighan & ritchie")
      assert(false, "I should have failed")
    } catch {
      case _: JsonMappingException => ()
    }
  }

  "getISBN" should "optionally return a title" in {
    assert(client.getISBN("9781530826605") contains "Make Your Own Neural Network")
    assert(client.getISBN("9781530826606").isEmpty)
  }

  "getCheckedOutTitles" should "return a list of titles" in {
    assert(client.getCheckedOutTitles(1).toSet === Set(
      "Programming in Scala",
      "Functional Programming in Scala"
    ))
    assert(client.getCheckedOutTitles(2).isEmpty)
    assert(client.getCheckedOutTitles(3) === List("Make Your Own Neural Network"))
  }
}
