package com.mygdx.game.gamestate.ability

import com.mygdx.game.gamestate.{Entity, EntityId, GameState, Outcome}
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{Vector2, WorldDirection}
import com.softwaremill.quicklens.ModifyPimp

trait Ability extends Entity {
  val params: AbilityParams

  val atlasRegionName: String

  val atlasRegionX: Int
  val atlasRegionY: Int
  val worldWidth: Int
  val worldHeight: Int
  val atlasRegionWidth: Int
  val atlasRegionHeight: Int

  val speed: Float = 0f

  def id: EntityId[Ability] = params.id

  def pos: Vector2 = params.pos

  def facingDirection: WorldDirection = {
    val angleDeg = params.facingVector.angleDeg

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

  def update(
      delta: Float,
      newPos: Vector2,
      gameState: GameState
  ): Outcome[Ability] = {
    for {
      ability <- this.updateFacingVector()
      ability <- Outcome(
        ability
          .modify(_.params.pos)
          .setTo(newPos)
      )
    } yield ability
  }

  private def updateFacingVector(): Outcome[Ability] = {
    Outcome(
      this
        .modify(_.params.facingVector)
        .setToIf(velocity.length > 0)(velocity)
    )
  }

  def velocity: Vector2 = {
    this.params.facingVector.normalized.multiply(this.speed)
  }

  def copy(params: AbilityParams): Ability
}
