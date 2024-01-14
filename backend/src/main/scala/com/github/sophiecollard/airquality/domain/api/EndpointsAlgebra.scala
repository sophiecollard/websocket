package com.github.sophiecollard.airquality.domain.api

import com.github.sophiecollard.airquality.domain.model.DailyAirQualityIndex.LocalAuthority
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

object EndpointsAlgebra {

  val pathPrefix: String = "/v1"

  lazy val getDailyAirQualityIndex: PublicEndpoint[Unit, Nothing, List[LocalAuthority], Any] =
    infallibleEndpoint
      .get
      .in("index" / "daily")
      .out(jsonBody[List[LocalAuthority]])
      .description("Fetch the latest daily air quality index")

}
