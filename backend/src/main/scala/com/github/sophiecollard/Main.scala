package com.github.sophiecollard

import cats.effect.{ExitCode, IO, IOApp}
import com.github.sophiecollard.livechat.domain.services.ChatService
import com.github.sophiecollard.livechat.interpreters.api.{ChatEndpoints, HealthCheckEndpoints}
import com.github.sophiecollard.livechat.interpreters.server.{Server, WebSocketServer}
import fs2._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val chatService = ChatService[IO]
    val (healthCheckEndpoints, chatWebsocketEndpoints) = (HealthCheckEndpoints[IO], ChatEndpoints[IO](chatService))
    val (server, wsServer) = (Server.builder(healthCheckEndpoints), WebSocketServer.builder(chatWebsocketEndpoints))
    Stream
      .eval(wsServer.build.use(_ => server.build.use(_ => IO.never)))
      .as(ExitCode.Success)
      .compile.last
      .map(_.getOrElse(ExitCode.Error))
  }
}
