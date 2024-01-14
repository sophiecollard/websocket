package com.github.sophiecollard.interpreters.clients

import cats.effect.IO
import cats.implicits._
import com.github.sophiecollard.domain.clients.{LondonAirClientAlgebra, LondonAirEndpointsAlgebra}
import com.github.sophiecollard.domain.error.AppError
import com.github.sophiecollard.domain.error.AppError.{ThirdPartyNotReachable, ThirdPartyRespondedWithUnexpectedStatusCode}
import com.github.sophiecollard.domain.model.DailyAirQualityIndex
import org.http4s.Status
import org.http4s.client.{Client, ConnectionFailure}
import org.typelevel.log4cats.Logger
import sttp.tapir.client.http4s.Http4sClientInterpreter

object LondonAirClient {

  private lazy val getDailyAirQualityIndexEndpoint =
    Http4sClientInterpreter[IO]().toRequest(
      e = LondonAirEndpointsAlgebra.getDailyAirQualityIndex,
      baseUri = Some(LondonAirEndpointsAlgebra.baseUri)
    )

  def apply(client: Client[IO], logger: Logger[IO]): LondonAirClientAlgebra[IO] =
    new LondonAirClientAlgebra[IO] {
      override def getDailyAirQualityIndex: IO[Either[AppError, DailyAirQualityIndex]] = {
        val (request, responseHandler) = getDailyAirQualityIndexEndpoint()
        client.run(request).use { response =>
          if (response.status == Status.Ok) {
            val errorMessage = s"Failed to decode daily air quality index returned from ${request.uri}"
            responseHandler(response).flatMap(handleDecodeResult[DailyAirQualityIndex](logger, errorMessage))
          } else {
            val errorMessage = s"London Air API responded with status code ${response.status.code} when attempting to fetch daily air quality index"
            ThirdPartyRespondedWithUnexpectedStatusCode(errorMessage).asLeft[DailyAirQualityIndex].pure[IO]
          }
        }.handleError(connectionFailureHandler[DailyAirQualityIndex])
      }

      def connectionFailureHandler[T]: Throwable => Either[AppError, T] = { case cf: ConnectionFailure =>
        ThirdPartyNotReachable(cf.getMessage).asLeft[T]
      }
    }

}
