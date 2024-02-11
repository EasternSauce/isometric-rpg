package com.mygdx.game.gamestate

case class Outcome[T](obj: T, events: List[Event] = Nil) {
  @inline final def map[B](f: T => B): Outcome[B] =
    Outcome(f(this.obj), events = events)

  @inline final def flatMap[B](f: T => Outcome[B]): Outcome[B] = {
    val newOutcome = f(obj)
    Outcome(newOutcome.obj, events ++ newOutcome.events)
  }

  def ++(f: T => Outcome[T]): Outcome[T] = {
    val newOutcome = f(obj)
    Outcome(newOutcome.obj, events ++ newOutcome.events)
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
