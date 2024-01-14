package com.github.sophiecollard.chat.domain.api

import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.model.{Id, Message, User}
import fs2.Pipe
import sttp.tapir.PublicEndpoint
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._

object ChatEndpointAlgebra {
  val pathPrefix: String = "/v1"

  def joinChat[F[_]]: PublicEndpoint[Id[User], Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]], Fs2Streams[F] with WebSockets] =
    infallibleEndpoint
      .get
      .in("chat")
      .in(query[Id[User]]("userId"))
      .out(webSocketBody[WebSocketEvent[String], CodecFormat.TextPlain, WebSocketEvent[Message], CodecFormat.TextPlain](Fs2Streams[F]))
}
