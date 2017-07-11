package com.github.smedbergm.biblioClient

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json4s.JValue
import org.json4s.jackson.Serialization.write
import org.json4s.jackson.JsonMethods.parse

// Please for the love of all that is holy don't write code like this.
class BiblioClient {
  private val client = HttpClients.createDefault()
  implicit val formats = org.json4s.DefaultFormats

  def getUser(username: String): User = {
    val requestBody = new StringEntity(write(Map("query" -> username)))
    val request = new HttpPost("http://localhost:8080/user")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    val users = parse(responseBody).extract[List[User]]
    users.head
  }

  def searchTitles(query: String): List[String] = {
    val requestBody = new StringEntity(write(Map("title" -> query)))
    val request = new HttpPost("http://localhost:8080/book")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    parse(responseBody).extract[List[JValue]].map{
      jValue => (jValue \ "title").extract[String]
    }
  }

  def searchAuthors(query: String): List[String] = {
    val requestBody = new StringEntity(write(Map("author" -> query)))
    val request = new HttpPost("http://localhost:8080/book")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    parse(responseBody).extract[List[JValue]].map{
      jValue => (jValue \ "author").extract[String]
    }
  }

  def searchByAuthor(query: String): List[String] = {
    val requestBody = new StringEntity(write(Map("author" -> query)))
    val request = new HttpPost("http://localhost:8080/book")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    parse(responseBody).extract[List[JValue]].map{
      jValue => (jValue \ "title").extract[String]
    }
  }

  def getISBN(isbn: String): Option[String] = {
    val requestBody = new StringEntity(write(Map("isbn" -> isbn)))
    val request = new HttpPost("http://localhost:8080/book")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    parse(responseBody).extract[List[JValue]].headOption match {
      case Some(jValue) => (jValue \ "title").extractOpt[String]
      case None => None
    }
  }

  def getCheckedOutTitles(userID: Long): List[String] = {
    val requestBody = new StringEntity(write(Map("userID" -> userID)))
    val request = new HttpPost("http://localhost:8080/user/items")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    val isbns = parse(responseBody).extract[List[String]]
    isbns.flatMap(isbn => getISBN(isbn))
  }
}

case class User(userID: Long, username: String, firstName: String, lastName: String)