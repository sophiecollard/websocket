package com.github.sophiecollard.chat.domain.services

import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.implicits._
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.model.{Id, Message, User}
import fs2.Stream

trait ChatService[F[_]] {
  def join(userId: Id[User], inputStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]]
  def leave(userId: Id[User]): F[Unit]
}

object ChatService {
  def apply[F[_]: Async]: ChatService[F] = new ChatService[F] {
    private var queues: Map[Id[User], Queue[F, WebSocketEvent[Message]]] = Map.empty

    override def join(userId: Id[User], inputStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]] = {
      for {
        // Create a new message queue for the user who just joined
        queue <- Stream.eval(Queue.unbounded[F, WebSocketEvent[Message]])
        // Add the user's message queue to the collection of queues
        _ = { queues = queues + (userId -> queue) }
        // Broadcast incoming messages by adding them onto every other user's queue
        incomingStream = inputStream.evalMap(event => queues.view.filterKeys(_ != userId).values.toList.traverse(_.offer(event)).as(event))
        outgoingStream = Stream.fromQueueUnterminated(queue)
        resultingStream <- outgoingStream merge incomingStream
      } yield resultingStream
    }

    override def leave(userId: Id[User]): F[Unit] =
      Async[F].delay({ queues = queues removed userId })
  }
}
