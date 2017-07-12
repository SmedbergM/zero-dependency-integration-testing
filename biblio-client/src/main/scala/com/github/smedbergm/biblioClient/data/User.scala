package com.github.smedbergm.biblioClient.data

case class User(id: Long, username: String, firstName: String, lastName: String)
case class Book(isbn: Long, title: String, author: String)