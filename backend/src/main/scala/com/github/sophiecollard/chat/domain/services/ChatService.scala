package com.github.sophiecollard.chat.domain.services

import cats.effect.kernel.Async
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.model.{Id, Message, User}
import fs2.Stream

import java.util.UUID
import scala.concurrent.duration._

trait ChatService[F[_]] {
  def join(userId: Id[User], messageStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]]
  def leave(userId: Id[User]): F[Unit]
}

object ChatService {
  private val adminUserId: Id[User] = Id(UUID.fromString("2d78f108-6b78-484b-ae35-65d83b14b7d8"))

  def apply[F[_]: Async]: ChatService[F] = new ChatService[F] {
    private var str: Stream[F, WebSocketEvent[Message]] =
      //(Stream.never.covary[F]: Stream[F, WebSocketEvent[Message]])
      Stream.awakeEvery(10.seconds).map(d => WebSocketEvent(Message(s"This chat have been live for $d", adminUserId))).covary[F]
        // Print every WebSocket event to facilitate debugging
        .evalTap(event => Async[F].delay(println(event)))

    override def join(userId: Id[User], messageStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]] = {
//      str = str interleave messageStream
//      str // Is this going to work with an immutable stream?
      messageStream
    }

    override def leave(userId: Id[User]): F[Unit] =
      ???
  }
}
