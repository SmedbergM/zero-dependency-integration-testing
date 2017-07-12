package com.github.smedbergm.biblioClient

import com.fasterxml.jackson.databind.JsonMappingException
import com.github.smedbergm.biblioClient.data.User
import org.scalatest.FlatSpec

class BiblioClientSpec extends FlatSpec {
  val client = new BiblioClient(BiblioServiceFake)

  "getUser" should "get a user" in {
    assert(client.getUser("edijkstra") contains User(1, "edijkstra@texas.edu","Edsger","Dijkstra"))

    assert(client.getUser("odersky").isEmpty)
  }

  "searchTitles" should "return a list of titles" in {
    assert(client.searchTitles("programming").toSet === Set(
      "The Self-Taught Programmer: The Definitive Guide to Programming Professionally",
      "Automate the Boring Stuff with Python: Practical Programming for Total Beginners",
      "Programming in Scala",
      "Functional Programming in Scala"
    ))

    assert(client.searchTitles("c++").isEmpty)
  }

  "searchAuthors" should "return a list of authors" in {
    assert(client.searchAuthors("martin").toSet === Set("Robert C. Martin", "Martin Odersky"))
    assert(client.searchAuthors("martin sky") === List("Martin Odersky"))
    assert(client.searchAuthors("martin blue sky").isEmpty)
    assert(client.searchAuthors("Kernighan & Ritchie").isEmpty)
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
    assert(client.searchByAuthor("kernighan & ritchie").isEmpty)
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
