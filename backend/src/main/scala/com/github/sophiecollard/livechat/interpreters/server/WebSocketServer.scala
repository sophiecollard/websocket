package com.github.sophiecollard.livechat.interpreters.server

import cats.effect.IO
import com.comcast.ip4s._
import com.github.sophiecollard.livechat.interpreters.api.ChatEndpoints
import org.http4s.Http
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2

object WebSocketServer {
  def builder(chatEndpoints: ChatEndpoints[IO]): EmberServerBuilder[IO] = {
    val withCORSPolicy = CORS.policy.withAllowOriginAll

    val webSocketApp: WebSocketBuilder2[IO] => Http[IO, IO] = {
      webSocketBuilder =>
        Router[IO](mappings =
          chatEndpoints.pathPrefix -> withCORSPolicy(chatEndpoints.webSocketRoutes(webSocketBuilder))
        ).orNotFound
    }

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpWebSocketApp(webSocketApp)
  }
}
