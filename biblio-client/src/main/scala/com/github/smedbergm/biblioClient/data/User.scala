package com.github.smedbergm.biblioClient.data

import scala.util.Try

import org.json4s.JValue

case class User(id: Long, username: String, firstName: String, lastName: String)
object User {
  private implicit val formats = org.json4s.DefaultFormats
  def parse(jValue: JValue): Option[User] = Try {
    val id = (jValue \ "userID").extract[Long]
    val username = (jValue \ "username").extract[String]
    val firstName = (jValue \ "firstName").extract[String]
    val lastName = (jValue \ "lastName").extract[String]
    User(id, username, firstName, lastName)
  }.toOption
}
case class Book(isbn: Long, title: String, author: String)
object Book {
  private implicit val formats = org.json4s.DefaultFormats
  def parse(jValue: JValue): Option[Book] = Try {
    val isbn = (jValue \ "isbn").extract[String].toLong
    val title = (jValue \ "title").extract[String]
    val author = (jValue \ "author").extract[String]
    Book(isbn, title, author)
  }.toOption
}