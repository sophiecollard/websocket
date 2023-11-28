package com.github.sophiecollard.livechat.interpreters.server

import cats.effect.IO
import com.comcast.ip4s._
import com.github.sophiecollard.livechat.interpreters.api.HealthCheckEndpoints
import org.http4s.Http
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS

object Server {
  def builder(healthCheckEndpoints: HealthCheckEndpoints[IO]): EmberServerBuilder[IO] = {
    val withCORSPolicy = CORS.policy.withAllowOriginAll

    val httpApp: Http[IO, IO] =
      Router[IO](mappings =
        healthCheckEndpoints.pathPrefix -> withCORSPolicy(healthCheckEndpoints.httpRoutes)
      ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
  }
}
