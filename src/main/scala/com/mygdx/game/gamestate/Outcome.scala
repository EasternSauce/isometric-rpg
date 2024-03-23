package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.event.physics.PhysicsEvent
import com.softwaremill.quicklens.ModifyPimp

case class Outcome[T](
    obj: T,
    gameStateEvents: List[GameStateEvent] = Nil,
    broadcastEvents: List[GameStateEvent] = Nil,
    physicsEvents: List[PhysicsEvent] = Nil
) {
  @inline final def map[B](f: T => B): Outcome[B] =
    Outcome(
      f(this.obj),
      broadcastEvents = broadcastEvents,
      gameStateEvents = gameStateEvents,
      physicsEvents = physicsEvents
    )

  @inline final def flatMap[B](f: T => Outcome[B]): Outcome[B] = {
    val newOutcome = f(obj)
    Outcome(
      newOutcome.obj,
      gameStateEvents ++ newOutcome.gameStateEvents,
      broadcastEvents ++ newOutcome.broadcastEvents,
      physicsEvents ++ newOutcome.physicsEvents
    )
  }

  def withGameStateEvents(gameEvents: List[GameStateEvent]): Outcome[T] = {
    this.modify(_.gameStateEvents).setTo(gameEvents)
  }

  def withBroadcastEvents(broadcastEvents: List[GameStateEvent]): Outcome[T] = {
    this.modify(_.broadcastEvents).setTo(broadcastEvents)
  }

  def withPhysicsEvents(physicsEvents: List[PhysicsEvent]): Outcome[T] = {
    this.modify(_.physicsEvents).setTo(physicsEvents)
  }

}

object Outcome {
  def when[T](
      obj: T
  )(condition: T => Boolean)(f: T => Outcome[T]): Outcome[T] = {
    if (condition(obj)) {
      f(obj)
    } else {
      Outcome(obj)
    }
  }
}
