package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.event.Event
import com.softwaremill.quicklens.ModifyPimp

case class Outcome[T](obj: T, events: List[Event] = Nil) {
  @inline final def map[B](f: T => B): Outcome[B] =
    Outcome(f(this.obj), events = events)

  @inline final def flatMap[B](f: T => Outcome[B]): Outcome[B] = {
    val newOutcome = f(obj)
    Outcome(newOutcome.obj, events ++ newOutcome.events)
  }

  def withEvents(events: List[Event]): Outcome[T] = {
    this.modify(_.events).setTo(events)
  }

  def withSideEffectsExtracted(
      sideEffectsCollector: GameStateSideEffectsCollector
  ): T = {
    sideEffectsCollector.events =
      sideEffectsCollector.events.appendedAll(events)

    obj
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
