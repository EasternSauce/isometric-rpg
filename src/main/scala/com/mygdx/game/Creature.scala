package com.mygdx.game

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.WorldDirection.WorldDirection
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
                     id: String,
                     x: Float,
                     y: Float,
                     textureName: String,
                     neutralStanceFrame: Int,
                     frameCount: Int,
                     frameDuration: Float,
                     dirMap: Map[WorldDirection, Int],
                     animationTimer: SimpleTimer,
                     moving: Boolean,
                     lastMovementDir: (Float, Float)
) {
  def update(delta: Float): Creature = {
    this.modify(_.animationTimer).using(_.update(delta))
  }

  def facingDirection: WorldDirection = {
    val angleDeg = new Vector2(lastMovementDir._1, lastMovementDir._2).angleDeg()
    println("angleDeg = " + angleDeg)
    angleDeg match {
      case angle if angle >= 45 && angle < 135 => WorldDirection.North
      case angle if angle >= 135 && angle < 225 => WorldDirection.West
      case angle if angle >= 225 && angle < 315 => WorldDirection.South
      case _ => WorldDirection.East
    }
  }

}
