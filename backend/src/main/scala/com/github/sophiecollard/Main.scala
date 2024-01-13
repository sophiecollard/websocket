package com.github.sophiecollard

import cats.effect.{ExitCode, IO, IOApp}
import com.github.sophiecollard.airquality.domain.model.DailyAirQualityIndex
import com.github.sophiecollard.airquality.interpreters.api.{Endpoints, WebSocketEndpoints}
import com.github.sophiecollard.airquality.interpreters.clients.LondonAirClient
import com.github.sophiecollard.airquality.interpreters.server.{Server, WebSocketServer}
import com.github.sophiecollard.chat.interpreters.api.ChatEndpoints
import fs2._
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val appStream: Stream[IO, ExitCode] =
      for {
        logger <- Stream.eval[IO, Logger[IO]](Slf4jLogger.create[IO])
        client <- Stream.resource(EmberClientBuilder.default[IO].withLogger(logger).build)
        londonAirClient = LondonAirClient(client, logger)
        maybeDailyAirQualityIndex <- Stream.eval(londonAirClient.getDailyAirQualityIndex)
        dailyAirQualityIndex <- Stream.emits[IO, DailyAirQualityIndex](maybeDailyAirQualityIndex.toSeq)
        _ <- Stream.eval(logger.info(s"Server ready with data: $dailyAirQualityIndex"))
        _ = println(s"Server ready with data: $dailyAirQualityIndex")
        (endpoints, wsEndpoints) = (Endpoints(dailyAirQualityIndex), WebSocketEndpoints(dailyAirQualityIndex))
        chatWebsocketEndpoints = ChatEndpoints[IO]
        (server, wsServer) = (Server.builder(endpoints), WebSocketServer.builder(wsEndpoints, chatWebsocketEndpoints))
        _ <- Stream.eval(wsServer.build.use(_ => server.build.use(_ => IO.never)))
      } yield ExitCode.Success
    appStream.compile.last.map(_.getOrElse(ExitCode.Error))
  }
}
