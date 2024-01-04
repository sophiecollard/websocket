package com.github.sophiecollard.airquality.interpreters

import cats.effect.IO
import cats.implicits._
import com.github.sophiecollard.airquality.domain.error.AppError
import com.github.sophiecollard.airquality.domain.error.AppError.{FailedToDecodeThirdPartyResponse, ThirdPartyRespondedWithAnError}
import org.typelevel.log4cats.Logger
import sttp.tapir.DecodeResult

import java.util.UUID

package object clients {

  def handleDecodeResult[T](logger: Logger[IO], errorMessage: String)(
    decodeResult: DecodeResult[Either[String, T]]
  ): IO[Either[AppError, T]] =
    decodeResult match {
      case DecodeResult.Value(payload) =>
        payload
          .leftMap[AppError](ThirdPartyRespondedWithAnError(_))
          .pure[IO]
      case failure: DecodeResult.Failure =>
        IO
          .delay(UUID.randomUUID())
          .flatTap(logId => logger.error(s"Decoding failure with log ID $logId: $failure"))
          .map(logId => FailedToDecodeThirdPartyResponse(errorMessage, logId).asLeft[T])
    }

}
