package com.mygdx.game.gamestate

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.screen.Input
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.view.tile.Tile
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
    params: CreatureParams
) {
  def update(delta: Float): Creature = {
    val vectorTowardsDest =
      new Vector2(params.destinationX - params.x, params.destinationY - params.y)

    if (vectorTowardsDest.len() > 0.2f) {
      val baseVelocity = 3f

      vectorTowardsDest.setLength(baseVelocity)
    }
    else {
      vectorTowardsDest.setLength(0f)
    }

    this
      .modify(_.params.animationTimer)
      .using(_.update(delta))
      .modify(_.params.velocityX)
      .setTo(vectorTowardsDest.x)
      .modify(_.params.velocityY)
      .setTo(vectorTowardsDest.y)
      .modify(_.params.lastVelocityX)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.x)
      .modify(_.params.lastVelocityY)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.y)
  }

  def facingDirection: WorldDirection = {
    val angleDeg =
      new Vector2(params.lastVelocityX, params.lastVelocityY)
        .angleDeg()

    angleDeg match {
      case angle if angle >= 45 && angle < 135  => WorldDirection.East
      case angle if angle >= 135 && angle < 225 => WorldDirection.North
      case angle if angle >= 225 && angle < 315 => WorldDirection.West
      case _                                    => WorldDirection.South
    }
  }

  def moving: Boolean = params.velocityX != 0 && params.velocityY != 0
}
