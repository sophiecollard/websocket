package com.github.sophiecollard.airquality.domain.error

import sttp.model.StatusCode

import java.util.UUID

sealed abstract class AppError(message: String, httpStatusCode: StatusCode)

object AppError {

  final case class FailedToDecodeThirdPartyResponse(message: String, logId: UUID)
    extends AppError(message, httpStatusCode = StatusCode.InternalServerError)

  final case class ThirdPartyNotReachable(message: String)
    extends AppError(message, httpStatusCode = StatusCode.FailedDependency)

  final case class ThirdPartyRespondedWithAnError(message: String)
    extends AppError(message, httpStatusCode = StatusCode.FailedDependency)

  final case class ThirdPartyRespondedWithUnexpectedStatusCode(message: String)
    extends AppError(message, httpStatusCode = StatusCode.FailedDependency)

}
