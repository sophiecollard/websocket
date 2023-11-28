package com.github.sophiecollard.livechat.domain.api

import sttp.model.StatusCode
import sttp.tapir._

object HealthCheckEndpointDefinitions {
  val pathPrefix: String = "/v1"

  def livenessProbe[F[_]]: PublicEndpoint[Unit, Nothing, StatusCode, Any] =
    infallibleEndpoint
      .get
      .in("alive")
      .out(statusCode)
      .description("Liveness probe")
}
