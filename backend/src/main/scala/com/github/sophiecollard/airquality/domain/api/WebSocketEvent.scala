package com.github.sophiecollard.airquality.domain.api

import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import sttp.tapir.{Codec, CodecFormat, DecodeResult, ValidationError, Validator}
import sttp.ws.WebSocketFrame

sealed trait WebSocketEvent[+A] {

  def isCloseFrame: Boolean =
    this match {
      case WebSocketEvent.Message(_) => false
      case WebSocketEvent.Close      => true
    }

}

object WebSocketEvent {

  final case class Message[A](value: A) extends WebSocketEvent[A]

  case object Close extends WebSocketEvent[Nothing]

  def apply[A](value: A): WebSocketEvent[A] = Message(value)

  implicit def codec[A](implicit de: Decoder[A], en: Encoder[A]): Codec[WebSocketFrame, WebSocketEvent[A], CodecFormat.TextPlain] =
    Codec.webSocketFrame.mapDecode[WebSocketEvent[A]] {
      case WebSocketFrame.Text(payload, true, None) =>
        parse(payload).fold(
          pf => DecodeResult.Error(pf.message, pf.underlying),
          json => json.as[A].fold(
            df => DecodeResult.Error(df.message, new RuntimeException(df.message)),
            value => DecodeResult.Value(Message(value))
          )
        )
      case WebSocketFrame.Close(_, _) =>
        DecodeResult.Value(Close)
      case wsf =>
        DecodeResult.InvalidValue(
          ValidationError(
            validator = Validator.enumeration(
              possibleValues = List(WebSocketFrame.text("<message-payload>"), WebSocketFrame.close)
            ),
            invalidValue = wsf
          ) :: Nil
        )
    } {
      case Message(payload) => WebSocketFrame.text(en(payload).noSpaces)
      case Close            => WebSocketFrame.close
    }

}
