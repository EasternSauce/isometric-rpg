package com.mygdx.game.gamestate

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.softwaremill.quicklens.ModifyPimp

import scala.util.chaining.scalaUtilChainingOps

case class Creature(
    params: CreatureParams
) {
  def update(delta: Float): Creature = {
    val vectorTowardsDest =
      new Vector2(
        params.destinationX - params.x,
        params.destinationY - params.y
      )

    if (vectorTowardsDest.len() > 0.2f) {
      val baseVelocity = 3f

      vectorTowardsDest.setLength(baseVelocity)
    } else {
      vectorTowardsDest.setLength(0f)
    }

    this
      .modify(_.params.animationTimer)
      .using(_.update(delta))
      .modify(_.params.lastPosTimer)
      .using(_.update(delta))
      .modify(_.params.velocityX)
      .setTo(vectorTowardsDest.x)
      .modify(_.params.velocityY)
      .setTo(vectorTowardsDest.y)
      .modify(_.params.lastVelocityX)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.x)
      .modify(_.params.lastVelocityY)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.y)
      .pipe(creature => {
        if (creature.params.lastPosTimer.time > 0.5f) {
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
            .pipe(creature => {
              val v1 =
                new Vector2(creature.params.lastPosX, creature.params.lastPosY)
              val v2 = new Vector2(creature.params.x, creature.params.y)

              if (v1.dst(v2) < 0.2f) {
                creature.forceStopMoving()

              } else {
                creature
              }
            })
            .modify(_.params.lastPosX)
            .setTo(params.x)
            .modify(_.params.lastPosY)
            .setTo(params.y)
        } else {
          creature
        }
      })
  }

  def forceStopMoving(): Creature = {
    this
      .modify(_.params.destinationX)
      .setTo(params.x)
      .modify(_.params.destinationY)
      .setTo(params.y)
  }

  def facingDirection: WorldDirection = {
    val angleDeg =
      new Vector2(params.lastVelocityX, params.lastVelocityY)
        .angleDeg()

    angleDeg match {
      case angle if angle >= 67.5 && angle < 112.5  => WorldDirection.East
      case angle if angle >= 112.5 && angle < 157.5 => WorldDirection.NorthEast
      case angle if angle >= 157.5 && angle < 202.5 => WorldDirection.North
      case angle if angle >= 202.5 && angle < 247.5 => WorldDirection.NorthWest
      case angle if angle >= 247.5 && angle < 292.5 => WorldDirection.West
      case angle if angle >= 292.5 && angle < 337.5 => WorldDirection.SouthWest
      case angle
          if (angle >= 337.5 && angle < 360) || (angle >= 0 && angle < 22.5) =>
        WorldDirection.South
      case angle if angle >= 22.5 && angle < 67.5 => WorldDirection.SouthEast
      case _                                      => throw new RuntimeException("unreachable")
    }
  }

  def moving: Boolean = params.velocityX != 0 && params.velocityY != 0
}
