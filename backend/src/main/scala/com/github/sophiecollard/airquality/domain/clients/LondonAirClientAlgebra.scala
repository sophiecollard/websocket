package com.github.sophiecollard.airquality.domain.clients

import com.github.sophiecollard.airquality.domain.error.AppError
import com.github.sophiecollard.airquality.domain.model.DailyAirQualityIndex

trait LondonAirClientAlgebra[F[_]] {

  def getDailyAirQualityIndex: F[Either[AppError, DailyAirQualityIndex]]

}
