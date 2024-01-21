package com.github.sophiecollard.chat.interpreters.api

import cats.effect.kernel.Async
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.api.ChatEndpointAlgebra
import com.github.sophiecollard.chat.domain.model.{Id, Message, User}
import com.github.sophiecollard.chat.domain.services.ChatService
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import java.util.UUID

final case class ChatEndpoints[F[_]](
  webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F],
  pathPrefix: String
)

object ChatEndpoints {
  private val adminUserId: Id[User] = Id(UUID.fromString("2d78f108-6b78-484b-ae35-65d83b14b7d8"))

  def apply[F[_]: Async](chatService: ChatService[F]): ChatEndpoints[F] = {
    val getMessages: ServerEndpoint[Fs2Streams[F] with WebSockets, F] =
      ChatEndpointAlgebra.joinChat.serverLogicPure { userId =>
        Right[Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]]] { inputStream =>
          // TODO Try and move some of this logic into the service

          // Issue a message when the user joins the chat
          val outputStream = Stream
            .emit[F, WebSocketEvent[Message]](WebSocketEvent.Message(Message(s"User ${userId.value} has joined the chat", adminUserId)))

          val messageStream = inputStream
            .map[WebSocketEvent[Message]] {
              // Echo back the user's messages
              case WebSocketEvent.Message(content) =>
                WebSocketEvent.Message(Message(content, userId))
              // Issue a message when the user leaves the chat
              case WebSocketEvent.Close =>
                WebSocketEvent.Message(Message(s"User ${userId.value} has left the chat", adminUserId))
            }
            // Print every event to facilitate debugging
            .evalTap(event => Async[F].delay(println(event)))
            // Consume the input stream until a Close event is received
            .takeWhile(!_.isCloseFrame)
            // Merge with output stream to display message about user joining
            .merge(outputStream)

          chatService.join(userId, messageStream)
        }
      }

    val webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] =
      Http4sServerInterpreter[F]().toWebSocketRoutes(getMessages)

    ChatEndpoints(webSocketRoutes, ChatEndpointAlgebra.pathPrefix)
  }
}
