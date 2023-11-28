package com.github.sophiecollard.domain.clients

import com.github.sophiecollard.domain.error.AppError
import com.github.sophiecollard.domain.model.DailyAirQualityIndex

trait LondonAirClientAlgebra[F[_]] {

  def getDailyAirQualityIndex: F[Either[AppError, DailyAirQualityIndex]]

}
