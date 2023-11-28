package com.github.sophiecollard.interpreters.server

import cats.effect.IO
import com.comcast.ip4s._
import com.github.sophiecollard.interpreters.api.WebSocketEndpoints
import org.http4s.Http
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2

object WebSocketServer {

  def builder(endpoints: WebSocketEndpoints): EmberServerBuilder[IO] = {
    val withCORSPolicy = CORS.policy.withAllowOriginAll

    val webSocketApp: WebSocketBuilder2[IO] => Http[IO, IO] = {
      webSocketBuilder =>
        Router[IO](mappings =
          endpoints.pathPrefix -> withCORSPolicy(endpoints.webSocketRoutes(webSocketBuilder))
        ).orNotFound
    }

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpWebSocketApp(webSocketApp)
  }

}
