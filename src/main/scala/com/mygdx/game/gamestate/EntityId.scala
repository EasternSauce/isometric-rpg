package com.mygdx.game.gamestate

class EntityId[T <: Entity](val value: String) {}

object EntityId {
  def apply[T <: Entity](value: String): EntityId[T] = new EntityId(value)
}
