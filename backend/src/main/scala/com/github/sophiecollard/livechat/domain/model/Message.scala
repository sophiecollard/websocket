package com.github.sophiecollard.livechat.domain.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class Message(
  contents: String,
  author: Id[User]
)

object Message {
  implicit val decoder: Decoder[Message] = deriveDecoder[Message]
  implicit val encoder: Encoder[Message] = deriveEncoder[Message]
}