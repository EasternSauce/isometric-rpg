package com.mygdx.game

object WorldDirection extends Enumeration {

  type WorldDirection = Value
  val West, East, North, South = Value

  def isHorizontal(value: WorldDirection): Boolean =
    value match {
      case West => true
      case East => true
      case _    => false
    }

  def isVertical(value: WorldDirection): Boolean =
    value match {
      case North => true
      case South => true
      case _     => false
    }
}
