package com.github.sophiecollard.livechat.interpreters.api

import cats.effect.kernel.Async
import com.github.sophiecollard.livechat.domain.api.HealthCheckEndpointDefinitions
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

final case class HealthCheckEndpoints[F[_]](httpRoutes: HttpRoutes[F], pathPrefix: String)

object HealthCheckEndpoints {
  def apply[F[_]: Async]: HealthCheckEndpoints[F] = {
    val livenessProbe: ServerEndpoint[Any, F] =
      HealthCheckEndpointDefinitions.livenessProbe.serverLogicPure { _ =>
        Right(StatusCode.Ok)
      }

    val httpRoutes: HttpRoutes[F] = Http4sServerInterpreter[F]().toRoutes(livenessProbe)

    HealthCheckEndpoints(httpRoutes, HealthCheckEndpointDefinitions.pathPrefix)
  }
}
