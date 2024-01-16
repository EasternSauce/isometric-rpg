package com.mygdx.game.gamestate

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
    params: CreatureParams
) {
  def update(delta: Float): Creature = {

    this.modify(_.params.animationTimer).using(_.update(delta))
  }

  def facingDirection: WorldDirection = {
    val angleDeg =
      new Vector2(params.lastMovementDir._1, params.lastMovementDir._2)
        .angleDeg()

    angleDeg match {
      case angle if angle >= 45 && angle < 135  => WorldDirection.East
      case angle if angle >= 135 && angle < 225 => WorldDirection.North
      case angle if angle >= 225 && angle < 315 => WorldDirection.West
      case _                                    => WorldDirection.South
    }
  }

}
