package com.github.sophiecollard.interpreters.api

import cats.effect.IO
import com.github.sophiecollard.domain.api.{WebSocketEndpointsAlgebra, WebSocketEvent}
import com.github.sophiecollard.domain.model.DailyAirQualityIndex.LocalAuthority
import com.github.sophiecollard.domain.model.DailyAirQualityIndex
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.duration._

final case class WebSocketEndpoints(
  webSocketRoutes: WebSocketBuilder2[IO] => HttpRoutes[IO],
  pathPrefix: String
)

object WebSocketEndpoints {

  def apply(dailyAirQualityIndex: DailyAirQualityIndex): WebSocketEndpoints = {
    val getDailyAirQualityIndex: ServerEndpoint[Fs2Streams[IO] with WebSockets, IO] =
      WebSocketEndpointsAlgebra.getDailyAirQualityIndex.serverLogicPure { _ =>
        Right[Nothing, Pipe[IO, WebSocketEvent[String], WebSocketEvent[LocalAuthority]]] { inputStream =>
          val outputStream = Stream
            .emits[IO, WebSocketEvent[LocalAuthority]](dailyAirQualityIndex.localAuthorities.map(WebSocketEvent.Message(_)))
            .spaced(1.second, startImmediately = true)
            // Close the output stream with a CloseEvent
            .append(Stream(WebSocketEvent.Close))
            .map(Some(_))

          inputStream
            // Consume the input stream until the client sends a CloseEvent
            .takeWhile(!_.isCloseFrame)
            .map(_ => Option.empty[WebSocketEvent[LocalAuthority]])
            .merge(outputStream)
            .unNone
        }
      }

    val webSocketRoutes: WebSocketBuilder2[IO] => HttpRoutes[IO] =
      Http4sServerInterpreter[IO]().toWebSocketRoutes(getDailyAirQualityIndex :: Nil)

    WebSocketEndpoints(webSocketRoutes, WebSocketEndpointsAlgebra.pathPrefix)
  }

}
