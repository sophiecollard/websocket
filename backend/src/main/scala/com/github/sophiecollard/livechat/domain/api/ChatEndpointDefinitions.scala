package com.github.sophiecollard.livechat.domain.api

import com.github.sophiecollard.livechat.domain.model.{Message, Username}
import fs2.Pipe
import sttp.tapir.PublicEndpoint
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._

object ChatEndpointDefinitions {
  val pathPrefix: String = "/v1"

  def joinChat[F[_]]: PublicEndpoint[Username, Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]], Fs2Streams[F] with WebSockets] =
    infallibleEndpoint
      .get
      .in("chat")
      .in(query[Username]("username"))
      .out(webSocketBody[WebSocketEvent[String], CodecFormat.TextPlain, WebSocketEvent[Message], CodecFormat.TextPlain](Fs2Streams[F]))
}
