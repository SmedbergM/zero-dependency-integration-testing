package com.github.smedbergm.biblioClient
import java.util.concurrent.atomic.AtomicReference
import scala.util.{Failure, Try}

import com.github.smedbergm.biblioClient.data.{User, Book}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods.parse

object BiblioService11Connector extends BiblioServiceInterface {
  private val client = HttpClients.createDefault()
  private implicit val formats = org.json4s.DefaultFormats
  private def uriBuilder = new URIBuilder()
    .setScheme("http")
    .setHost("localhost")
    .setPort(8180)

  private val p_accessToken = new AtomicReference[String]()

  def authenticate(username: String, password: String): Try[Unit] = Try {
    val uri = uriBuilder.setPath("/auth").setParameter("username", username).setParameter("password",password).build()
    val request = new HttpGet(uri)
    val response = client.execute(request)
    val responseBody = EntityUtils.toString(response.getEntity)
    response.close()
    val accessToken = (parse(responseBody) \ "accessToken").extract[String]
    p_accessToken.set(accessToken)
  }
  def logout(): Unit = p_accessToken.set(null)

  override def getUsers(username: String): Try[List[User]] = {
    Option(p_accessToken.get) match {
      case None => Failure(new IllegalStateException("No authentication credentials found"))
      case Some(token) => Try {
        val uri = uriBuilder
          .setPath("/api/user")
          .setParameter("search", username)
          .build()
        val request = new HttpGet(uri)
        request.addHeader("Authorization", s"Bearer $token")
        val response = client.execute(request)
        val responseBody = EntityUtils.toString(response.getEntity)
        response.close()
        parse(responseBody).extract[List[JValue]].map(User.parse).collect {
          case Some(user) => user
        }
      }
    }
  }

  override def searchBooks(titleQuery: Option[String], authorQuery: Option[String], isbnQuery: Option[String]): Try[List[Book]] = {
    Option(p_accessToken.get) match {
      case None => Failure(new IllegalStateException("No authentication credentials found"))
      case Some(token) => Try {
        val builder = uriBuilder.setPath("/api/book")
        titleQuery.foreach(query => builder.setParameter("title", query))
        authorQuery.foreach(query => builder.setParameter("author", query))
        isbnQuery.foreach(query => builder.setParameter("isbn", query))
        val uri = builder.build()
        val request = new HttpGet(uri)
        request.addHeader("Authorization", s"Bearer $token")
        val response = client.execute(request)
        val responseBody = EntityUtils.toString(response.getEntity)
        response.close()
        parse(responseBody).extract[List[JValue]].map(Book.parse).collect {
          case Some(book) => book
        }
      }
    }
  }

  override def getCheckedOutItems(userID: Long): Try[List[Book]] = {
    Option(p_accessToken.get) match {
      case None => Failure(new IllegalStateException("No authentication credentials found"))
      case Some(token) => Try {
        val uri = uriBuilder
          .setPath("/api/user/items")
          .setParameter("userID", userID.toString)
          .build()
        val request = new HttpGet(uri)
        request.addHeader("Authorization", s"Bearer $token")
        val response = client.execute(request)
        val responseBody = EntityUtils.toString(response.getEntity)
        response.close()
        parse(responseBody).extract[List[JValue]].map(Book.parse).collect {
          case Some(book) => book
        }
      }
    }
  }
}
