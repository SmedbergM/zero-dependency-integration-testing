package com.github.smedbergm.biblioClient

import scala.util.{Try, Success}

import com.github.smedbergm.biblioClient.data.{User, Book}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

trait BiblioServiceInterface {
  def getUsers(username: String): Try[List[User]]
  def searchBooks(
    titleQuery: Option[String] = None,
    authorQuery: Option[String] = None,
    isbnQuery: Option[String] = None
  ): Try[List[Book]]
  def getCheckedOutItems(userID: Long): Try[List[Book]]
}

object BiblioService10Connector extends BiblioServiceInterface {
  private val client = HttpClients.createDefault()
  private val baseURL = "http://localhost:8080"
  private implicit val formats = org.json4s.DefaultFormats

  override def getUsers(username: String): Try[List[User]] = Try {
    val requestBody = new StringEntity(write(Map("query" -> username)))
    val request = new HttpPost(s"$baseURL/user")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    response.close()
    parse(responseBody).extract[List[JValue]].map { jValue =>
      val id = (jValue \ "userID").extract[Long]
      val uname = (jValue \ "username").extract[String]
      val firstName = (jValue \ "firstName").extract[String]
      val lastName = (jValue \ "lastName").extract[String]
      User(id, uname, firstName, lastName)
    }
  }

  override def searchBooks(
    titleQuery: Option[String],
    authorQuery: Option[String],
    isbnQuery: Option[String]
  ): Try[List[Book]] = Try {
    val requestBody = new StringEntity(write(Map(
      "title" -> titleQuery,
      "author" -> authorQuery,
      "isbn" -> isbnQuery
    ).collect{
      case (key, Some(query)) => key -> query
    }))
    val request = new HttpPost(s"$baseURL/book")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    response.close()
    parse(responseBody).extract[List[JValue]].map { jValue =>
      val isbn = (jValue \ "isbn").extract[String].toLong
      val title = (jValue \ "title").extract[String]
      val author = (jValue \ "author").extract[String]
      Book(isbn, title, author)
    }
  }

  override def getCheckedOutItems(userID: Long): Try[List[Book]] = Try {
    val requestBody = new StringEntity(write(Map("userID" -> userID)))
    val request = new HttpPost("http://localhost:8080/user/items")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    response.close()
    parse(responseBody).extract[List[String]]
  }.map { isbns =>
    isbns.map(isbn => searchBooks(isbnQuery = Some(isbn))).collect {
      case Success(books) => books
    }.flatten
  }
}
