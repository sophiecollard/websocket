package com.github.sophiecollard.domain.model

import com.github.sophiecollard.domain.model.DailyAirQualityIndex.LocalAuthority
import enumeratum.EnumEntry.Uppercase
import enumeratum._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

final case class DailyAirQualityIndex(localAuthorities: List[LocalAuthority])

object DailyAirQualityIndex {

  final case class LocalAuthority(name: String, sites: List[Site])

  object LocalAuthority {

    implicit val decoder: Decoder[LocalAuthority] =
      Decoder.instance { cursor =>
        for {
          name <- cursor.get[String]("@LocalAuthorityName")
          sites <- cursor.get[List[Site]]("Site").orElse(Right(Nil))
        } yield LocalAuthority(name, sites)
      }

    implicit val encoder: Encoder[LocalAuthority] =
      Encoder.instance { localAuthority =>
        Json.obj(fields =
          "@LocalAuthorityName" := localAuthority.name,
          "Site" := localAuthority.sites
        )
      }

  }

  final case class Site(
    name: String,
    `type`: SiteType,
    latitude: Double,
    longitude: Double,
    species: List[Species]
  )

  object Site {

    implicit val decoder: Decoder[Site] =
      Decoder.instance { cursor =>
        for {
          name <- cursor.get[String]("@SiteName")
          type_ <- cursor.get[SiteType]("@SiteType")
          latitude <- cursor.get[Double]("@Latitude")
          longitude <- cursor.get[Double]("@Longitude")
          species <- cursor.get[List[Species]]("Species").orElse(Right(Nil))
        } yield Site(name, type_, latitude, longitude, species)
      }

    implicit val encoder: Encoder[Site] =
      Encoder.instance { site =>
        Json.obj(fields =
          "@SiteName" := site.name,
          "@SiteType" := site.`type`,
          "@Latitude" := site.latitude,
          "@Longitude" := site.longitude,
          "Species" := site.species
        )
      }

  }

  sealed abstract class SiteType(entryName: String) extends EnumEntry

  object SiteType extends Enum[SiteType] with CirceEnum[SiteType] {

    case object Industrial extends SiteType("Industrial")
    case object Kerbside extends SiteType("Kerbside")
    case object Roadside extends SiteType("Roadside")
    case object Suburban extends SiteType("Suburban")
    case object UrbanBackground extends SiteType("Urban Background")

    override val values: IndexedSeq[SiteType] = findValues

  }

  final case class Species(code: SpeciesCode)

  object Species {

    implicit val decoder: Decoder[Species] =
      Decoder.instance { cursor =>
        for {
          code <- cursor.get[SpeciesCode]("@SpeciesCode")
        } yield Species(code)
      }

    implicit val encoder: Encoder[Species] =
      Encoder.instance { species =>
        Json.obj(fields =
          "@SpeciesCode" := species.code
        )
      }

  }

  sealed trait SpeciesCode extends EnumEntry with Uppercase

  object SpeciesCode extends Enum[SpeciesCode] with CirceEnum[SpeciesCode] {

    case object CO   extends SpeciesCode
    case object NO2  extends SpeciesCode
    case object O3   extends SpeciesCode
    case object PM10 extends SpeciesCode
    case object PM25 extends SpeciesCode
    case object SO2  extends SpeciesCode

    override val values: IndexedSeq[SpeciesCode] = findValues

  }

  implicit val decoder: Decoder[DailyAirQualityIndex] =
    Decoder.instance { cursor =>
      cursor
        .downField("DailyAirQualityIndex")
        .get[List[LocalAuthority]]("LocalAuthority")
        .map(apply)
    }

  implicit val encoder: Encoder[DailyAirQualityIndex] =
    Encoder.instance { daqi =>
      Json.obj(fields =
        "DailyAirQualityIndex" := Json.obj(fields =
          "LocalAuthority" := daqi.localAuthorities
        )
      )
    }

}
