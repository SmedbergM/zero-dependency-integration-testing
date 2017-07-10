package com.github.smedbergm.biblio.handlers

import java.time.Instant
import scala.collection.concurrent.TrieMap
import scala.util.hashing.MurmurHash3
import scalaz.concurrent.Task

import com.github.smedbergm.biblio.json.JsonSupport
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Request, Response}
import org.http4s.dsl._

object AuthHandler extends JsonSupport {
  def handle(username: String, password: String): Task[Response] = {
    val h = Hashable(username, password, Instant.now())
    val accessToken = f"${MurmurHash3.productHash(h)}%08x"
    AuthCache.accessTokens.put(accessToken, username -> Instant.now().plusSeconds(600))
    Ok(write(ResponseBody(username, accessToken)))
  }
  private case class Hashable(username: String, password: String, timestamp: Instant)
  private case class ResponseBody(username: String, accessToken: String)
}

object AuthCache {
  // Key: Access Token
  // Value: Username
  val accessTokens = new TrieMap[String,(String, Instant)]()
}

trait Authentication {
  def verify(request: Request): Task[String] = {
    val now = Instant.now()
    val optAccessToken: Option[String] = for {
      header <- request.headers.get(CaseInsensitiveString("Authorization"))
        if header.value.startsWith("Bearer ")
      accessToken = header.value.stripPrefix("Bearer ")
      _ = AuthCache.accessTokens.get(accessToken) match {
        case Some((username, expiration)) if expiration.isAfter(now) =>
          AuthCache.accessTokens.update(accessToken, username -> now.plusSeconds(600))
        case Some(_) => AuthCache.accessTokens.remove(accessToken)
        case _ => ()
      }
    } yield accessToken
    optAccessToken.map(Task.now).getOrElse(Task.fail(UnauthorizedException("Invalid access token.")))
  }
}