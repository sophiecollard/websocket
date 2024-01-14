package com.github.sophiecollard.chat.interpreters.api

import cats.effect.kernel.Async
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.api.ChatEndpointAlgebra
import com.github.sophiecollard.chat.domain.model.Message
import fs2.{Pipe, Stream}
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
        println(s"User $userId has joined the chat") // TODO Remove
        Right[Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]]] { inputStream =>
          val outputStream = Stream
            .emit[F, WebSocketEvent[Message]](WebSocketEvent.Message(Message(s"User $userId has joined the chat", userId)))

          inputStream
            // Consume the input stream until a Close event is received
            .takeWhile(!_.isCloseFrame)
            // Extract the Message event payloads from the stream
            .collect { case WebSocketEvent.Message(msg) => msg }
            // Print received messages to the console to help with debugging
            .evalTap(msg => Async[F].delay(println(s"${userId.value}: $msg")))
            // Echo back the Messages
            .map { content => WebSocketEvent.Message(Message(content, userId)) }
            // Merge with output stream to display message about user joining
            .merge(outputStream)
        }
      }

    val webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] =
      Http4sServerInterpreter[F]().toWebSocketRoutes(getMessages)

    ChatEndpoints(webSocketRoutes, ChatEndpointAlgebra.pathPrefix)
  }
}
