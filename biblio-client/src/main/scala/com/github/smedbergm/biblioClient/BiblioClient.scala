package com.github.smedbergm.biblioClient

import com.github.smedbergm.biblioClient.data.User

class BiblioClient(connector: BiblioServiceInterface) {
  def getUser(username: String): Option[User] = connector
    .getUsers(username).toOption.flatMap(_.headOption)

  def searchTitles(query: String): List[String] = {
    val books = connector
      .searchBooks(titleQuery = Some(query))
      .getOrElse(List.empty)
    books.map(_.title)
  }

  def searchAuthors(query: String): List[String] = {
    val books = connector
      .searchBooks(authorQuery = Some(query))
      .getOrElse(List.empty)
    books.map(_.author)
  }

  def searchByAuthor(query: String): List[String] = {
    val books = connector
      .searchBooks(authorQuery = Some(query))
      .getOrElse(List.empty)
    books.map(_.title)
  }

  def getISBN(isbn: String): Option[String] = {
    val books = connector
      .searchBooks(isbnQuery = Some(isbn))
      .getOrElse(List.empty)
    books.map(_.title).headOption
  }

  def getCheckedOutTitles(userID: Long): List[String] = connector
    .getCheckedOutItems(userID).getOrElse(List.empty).map(_.title)
}

