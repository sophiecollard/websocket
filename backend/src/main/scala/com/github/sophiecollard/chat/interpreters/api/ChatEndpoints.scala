package com.github.sophiecollard.chat.interpreters.api

import cats.effect.kernel.Async
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.api.ChatEndpointAlgebra
import com.github.sophiecollard.chat.domain.model.Message
import fs2.Pipe
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

final case class ChatEndpoints[F[_]](
  webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F],
  pathPrefix: String
)

object ChatEndpoints {
  def apply[F[_]: Async]: ChatEndpoints[F] = {
    val getMessages: ServerEndpoint[Fs2Streams[F] with WebSockets, F] =
      ChatEndpointAlgebra.joinChat.serverLogicPure { userId =>
        Right[Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]]] { inputStream =>
          inputStream
            // Consume the input stream until a Close event is received
            .takeWhile(!_.isCloseFrame)
            // Extract the Message event payloads from the stream
            .collect { case WebSocketEvent.Message(msg) => msg }
            // Echo back the Message events
            .map { content => WebSocketEvent.Message(Message(content, userId)) }
        }
      }

    val webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] =
      Http4sServerInterpreter[F]().toWebSocketRoutes(getMessages)

    ChatEndpoints(webSocketRoutes, ChatEndpointAlgebra.pathPrefix)
  }
}
