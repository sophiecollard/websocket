package com.github.sophiecollard.domain.clients

import com.github.sophiecollard.domain.model.DailyAirQualityIndex
import org.http4s.implicits._
import org.http4s.Uri
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

object LondonAirEndpointsAlgebra {

  val baseUri: Uri = uri"https://api.erg.ic.ac.uk"

  lazy val getDailyAirQualityIndex: Endpoint[Unit, Unit, String, DailyAirQualityIndex, Any] =
    endpoint
      .get
      .in("AirQuality" / "Daily" / "MonitoringIndex" / "Latest" / "GroupName=London" / "Json")
      .out(jsonBody[DailyAirQualityIndex])
      .errorOut(stringBody)

}
