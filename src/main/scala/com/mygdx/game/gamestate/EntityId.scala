package com.mygdx.game.gamestate

class EntityId[T <: Entity](val value: String) {
  override def toString: String = value
}

object EntityId {
  def apply[T <: Entity](value: String): EntityId[T] = new EntityId(value)
}
