package com.github.sophiecollard.airquality.domain.api

import com.github.sophiecollard.airquality.domain.model.DailyAirQualityIndex.LocalAuthority
import fs2.Pipe
import sttp.capabilities.fs2.Fs2Streams
import sttp.capabilities.WebSockets
import sttp.tapir._

object WebSocketEndpointsAlgebra {

  val pathPrefix: String = "/v1"

  def getDailyAirQualityIndex[F[_]]: PublicEndpoint[Unit, Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[LocalAuthority]], Fs2Streams[F] with WebSockets] =
    infallibleEndpoint
      .get
      .in("index" / "daily")
      .out(webSocketBody[WebSocketEvent[String], CodecFormat.TextPlain, WebSocketEvent[LocalAuthority], CodecFormat.TextPlain](Fs2Streams[F]))
      .description("Fetch the latest daily air quality index as a stream")

}
