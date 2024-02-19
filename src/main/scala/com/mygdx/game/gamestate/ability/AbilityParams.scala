package com.mygdx.game.gamestate.ability

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.util.Vector2

case class AbilityParams(
    id: EntityId[Ability],
    pos: Vector2,
    velocity: Vector2 = Vector2(0, 0),
    facingVector: Vector2 = Vector2(1, 0)
) {}
