package com.mygdx.game

import scala.util.Random

object WorldDirection extends Enumeration {

  def isHorizontal(value: WorldDirection): Boolean =
    value match {
      case West  => true
      case East => true
      case _     => false
    }

  def isVertical(value: WorldDirection): Boolean =
    value match {
      case North   => true
      case South => true
      case _    => false
    }

  type WorldDirection = Value
  val West, East, North, South = Value
}