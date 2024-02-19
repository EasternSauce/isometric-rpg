package com.mygdx.game.gamestate

case class EntityId[T <: Entity](value: String) {
  override def toString: String = value
}

object EntityId {
  def apply[T <: Entity](value: String): EntityId[T] = new EntityId(value)
}
