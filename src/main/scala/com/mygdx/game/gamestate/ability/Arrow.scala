package com.mygdx.game.gamestate.ability
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.util.WorldDirection.WorldDirection

case class Arrow(params: AbilityParams) extends Ability {
  override val atlasRegionName: String = "projectiles"
  override val worldDirectionRegionMapping: Map[WorldDirection, (Int, Int)] =
    Map(
      WorldDirection.NorthWest -> (0, 0),
      WorldDirection.North -> (64, 0),
      WorldDirection.NorthEast -> (128, 0),
      WorldDirection.East -> (192, 0),
      WorldDirection.SouthEast -> (256, 0),
      WorldDirection.South -> (320, 0),
      WorldDirection.SouthWest -> (384, 0),
      WorldDirection.West -> (448, 0)
    )
  override val atlasRegionWidth: Int = 64
  override val atlasRegionHeight: Int = 64

  override def copy(params: AbilityParams): Ability = {
    Arrow(params)
  }
}
