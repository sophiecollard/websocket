package com.github.sophiecollard.livechat.domain.services

import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.implicits._
import com.github.sophiecollard.livechat.domain.api.WebSocketEvent
import com.github.sophiecollard.livechat.domain.model.{Id, Message, User}
import fs2.Stream

import java.util.UUID

trait ChatService[F[_]] {
  def join(userId: Id[User], incomingUserMessageStream: Stream[F, WebSocketEvent[String]]): Stream[F, WebSocketEvent[Message]]
  def leave(userId: Id[User]): F[Unit]
}

object ChatService {
  private val adminUserId: Id[User] = Id(UUID.fromString("2d78f108-6b78-484b-ae35-65d83b14b7d8"))

  def apply[F[_]: Async]: ChatService[F] = new ChatService[F] {
    private var state: Map[Id[User], Queue[F, WebSocketEvent[Message]]] = Map.empty

    override def join(userId: Id[User], incomingUserMessageStream: Stream[F, WebSocketEvent[String]]): Stream[F, WebSocketEvent[Message]] = {
      for {
        // Create a new message queue for the user joining the chat
        queue <- Stream.eval(Queue.unbounded[F, WebSocketEvent[Message]])
        // Add the user's message queue to the state
        _ <- Stream.eval(Async[F].delay { state = state + (userId -> queue) })
        // Blend user messages with admin messages about users joining and/or leaving the chat
        incomingBlendedMessageStream = processIncomingUserMessageStream(userId, incomingUserMessageStream)
        // Broadcast incoming user and admin messages by adding them onto every other user's queue
        enqueuedMessageStream = incomingBlendedMessageStream.evalMap(event => state.view.filterKeys(_ != userId).values.toList.traverse(_.offer(event)).as(event))
        // Stream messages from the user's queue
        dequeuedMessageStream = Stream.fromQueueUnterminated(queue)
        // Merge the enqueued and dequeued message streams into a single stream
        outgoingMessageStream <- dequeuedMessageStream merge enqueuedMessageStream
      } yield outgoingMessageStream
    }

    private def processIncomingUserMessageStream(userId: Id[User], incomingUserMessageStream: Stream[F, WebSocketEvent[String]]): Stream[F, WebSocketEvent[Message]] = {
      // Issue a message when a new user joins the chat
      val adminMessageStream = Stream
        .emit[F, WebSocketEvent[Message]](WebSocketEvent.Message(Message(s"User ${userId.value} has joined the chat", adminUserId)))

      incomingUserMessageStream
        .flatMap[F, WebSocketEvent[Message]] {
          // Echo back the user's messages
          case WebSocketEvent.Message(content) =>
            Stream.emit(WebSocketEvent.Message(Message(content, userId)))
          // Issue a message when a user leaves the chat
          case WebSocketEvent.Close =>
            Stream.emits(
              List(
                WebSocketEvent.Message(Message(s"User ${userId.value} has left the chat", adminUserId)),
                WebSocketEvent.Close
              )
            )
        }
        // Consume the input stream until a Close event is received
        .takeWhile(!_.isCloseFrame)
        // Merge with output stream to display message about user joining
        .merge(adminMessageStream)
    }

    override def leave(userId: Id[User]): F[Unit] =
      Async[F].delay { state = state removed userId }
  }
}
