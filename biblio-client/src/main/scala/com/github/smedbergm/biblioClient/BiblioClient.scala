package com.github.smedbergm.biblioClient

import com.github.smedbergm.biblioClient.data.User

class BiblioClient {
  def getUser(username: String): User = BiblioServiceConnector.getUsers(username).head

  def searchTitles(query: String): List[String] = BiblioServiceConnector
    .searchBooks(titleQuery = Some(query))
    .map(_.title)

  def searchAuthors(query: String): List[String] = BiblioServiceConnector
    .searchBooks(authorQuery = Some(query)).map(_.author)

  def searchByAuthor(query: String): List[String] = BiblioServiceConnector
    .searchBooks(authorQuery = Some(query)).map(_.title)

  def getISBN(isbn: String): Option[String] = BiblioServiceConnector
    .searchBooks(isbnQuery = Some(isbn)).map(_.title).headOption

  def getCheckedOutTitles(userID: Long): List[String] = BiblioServiceConnector
    .getCheckedOutItems(userID).map(_.title)
}

