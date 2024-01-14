package com.github.sophiecollard.chat.domain.model

import io.circe.{Decoder, Encoder}
import sttp.tapir.{Codec, CodecFormat}

import java.util.UUID

final case class Id[A](value: UUID)

object Id {
  implicit def idDecoder[A]: Decoder[Id[A]] = Decoder[UUID].map(uuidVal => Id(uuidVal))
  implicit def idEncoder[A]: Encoder[Id[A]] = Encoder[UUID].contramap(_.value)
  implicit def codec[A]: Codec[String, Id[A], CodecFormat.TextPlain] = Codec.uuid.map(apply[A](_))(_.value)
}