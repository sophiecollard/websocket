package com.github.sophiecollard.chat.domain.services

import cats.effect.kernel.Async
import cats.effect.std.{Dispatcher, Queue}
import cats.implicits._
import com.github.sophiecollard.airquality.domain.api.WebSocketEvent
import com.github.sophiecollard.chat.domain.model.{Id, Message, User}
import fs2.Stream
import fs2.concurrent.Channel

import java.util.UUID

trait ChatService[F[_]] {
  def join(userId: Id[User], inputStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]]
  def leave(userId: Id[User]): F[Unit]
}

object ChatService {
  private val adminUserId: Id[User] = Id(UUID.fromString("2d78f108-6b78-484b-ae35-65d83b14b7d8"))

  def apply[F[_]: Async]: ChatService[F] = new ChatService[F] {
    private var stateA: Map[Id[User], Channel[F, WebSocketEvent[Message]]] = Map.empty
    private var queues: Map[Id[User], Queue[F, WebSocketEvent[Message]]] = Map.empty

    override def join(userId: Id[User], inputStream: Stream[F, WebSocketEvent[Message]]): Stream[F, WebSocketEvent[Message]] = {
      for {
        // Define a new channel for this user
        channel <- Stream.eval(Channel.unbounded[F, WebSocketEvent[Message]])
        _ = { stateA = stateA + (userId -> channel) }
        // Define a new queue for this user
        //dispatcher <- Stream.eval(Dispatcher.sequential[F])
        queue <- Stream.eval(Queue.unbounded[F, WebSocketEvent[Message]])
        _ = { queues = queues + (userId -> queue) }
        // Enqueue incoming messages onto the user's queue
        //_ <- inputStream.evalMap(event => queue.offer(event))
        // Enqueue incoming messages onto every user's queue
        _ <- inputStream.evalMap(event => queues.values.toList.traverse(_.offer(event)))
        outputStream <- Stream.fromQueueUnterminated(queue)
      } yield outputStream
    }

    override def leave(userId: Id[User]): F[Unit] =
      ???
  }
}
