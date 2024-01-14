package com.github.sophiecollard.interpreters.api

import cats.effect.IO
import com.github.sophiecollard.domain.api.EndpointsAlgebra
import com.github.sophiecollard.domain.model.DailyAirQualityIndex
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

final case class Endpoints(
  httpRoutes: HttpRoutes[IO],
  pathPrefix: String
)

object Endpoints {
  
  def apply(dailyAirQualityIndex: DailyAirQualityIndex): Endpoints = {
    val getDailyAirQualityIndex: ServerEndpoint[Any, IO] =
      EndpointsAlgebra.getDailyAirQualityIndex.serverLogicPure { _ =>
        Right(dailyAirQualityIndex.localAuthorities)
      }

    val httpRoutes: HttpRoutes[IO] =
      Http4sServerInterpreter[IO]().toRoutes(getDailyAirQualityIndex :: Nil)

    Endpoints(httpRoutes, EndpointsAlgebra.pathPrefix)
  }
  
}
