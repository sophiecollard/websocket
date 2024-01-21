package com.github.sophiecollard.livechat.domain.model

import io.circe.{Decoder, Encoder}
import sttp.tapir.{Codec, CodecFormat}

final case class Username(value: String) extends AnyVal

object Username {
  implicit val codec: Codec[String, Username, CodecFormat.TextPlain] = Codec.string.map(apply(_))(_.value)
  implicit val decoder: Decoder[Username] = Decoder[String].map(apply)
  implicit val encoder: Encoder[Username] = Encoder[String].contramap(_.value)
}
