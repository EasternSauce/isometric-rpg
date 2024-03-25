package com.mygdx.game.gamestate.ability

case class Arrow(params: AbilityParams) extends Ability {
  override val atlasRegionName: String = "projectiles"

  override val atlasRegionX: Int = 256
  override val atlasRegionY: Int = 0
  override val worldWidth: Int = 48
  override val worldHeight: Int = 48
  override val atlasRegionWidth: Int = 64
  override val atlasRegionHeight: Int = 64

  override val speed: Float = 12f

  override def destroyedOnContact: Boolean = true

  override def copy(params: AbilityParams): Ability = {
    Arrow(params)
  }

}
