package com.github.sophiecollard.livechat.interpreters.api

import cats.effect.kernel.Async
import com.github.sophiecollard.livechat.domain.api.{ChatEndpointDefinitions, WebSocketEvent}
import com.github.sophiecollard.livechat.domain.model.Message
import com.github.sophiecollard.livechat.domain.services.ChatService
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
  def apply[F[_]: Async](chatService: ChatService[F]): ChatEndpoints[F] = {
    val getMessages: ServerEndpoint[Fs2Streams[F] with WebSockets, F] =
      ChatEndpointDefinitions.joinChat.serverLogicPure { userId =>
        Right[Nothing, Pipe[F, WebSocketEvent[String], WebSocketEvent[Message]]] { inputStream =>
          chatService.join(userId, inputStream)
        }
      }

    val webSocketRoutes: WebSocketBuilder2[F] => HttpRoutes[F] =
      Http4sServerInterpreter[F]().toWebSocketRoutes(getMessages)

    ChatEndpoints(webSocketRoutes, ChatEndpointDefinitions.pathPrefix)
  }
}
