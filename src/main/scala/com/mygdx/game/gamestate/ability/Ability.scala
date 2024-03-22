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
    WorldDirection.fromVector(params.facingVector)
  }

  def update(
      delta: Float,
      newPos: Option[Vector2],
      gameState: GameState
  ): Outcome[Ability] = {
    for {
      ability <- this.updateFacingVector()
      ability <- Outcome.when(ability)(_ => newPos.nonEmpty)(ability =>
        Outcome(
          ability
            .modify(_.params.pos)
            .setTo(newPos.get)
        )
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

  def destroyedOnContact: Boolean

  def copy(params: AbilityParams): Ability
}
