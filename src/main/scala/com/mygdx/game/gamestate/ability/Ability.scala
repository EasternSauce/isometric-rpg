package com.mygdx.game.gamestate.ability

import com.mygdx.game.gamestate.Entity
import com.mygdx.game.util.Vector2

trait Ability extends Entity {
  val params: AbilityParams

  def pos: Vector2 = params.pos
}
