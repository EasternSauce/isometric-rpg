package com.mygdx.game.gamestate

case class Outcome[T](obj: T, events: List[Event] = Nil) {
  @inline final def map[B](f: T => B): Outcome[B] =
    Outcome(f(this.obj), events = events)

  def ++(f: T => Outcome[T]): Outcome[T] = {
    val newOutcome = f(obj)
    Outcome(newOutcome.obj, events ++ newOutcome.events)
  }

}
