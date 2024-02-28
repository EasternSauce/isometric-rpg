package com.mygdx.game.gamestate.ability

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.util.Vector2

case class AbilityParams(
    id: EntityId[Ability],
    creatureId: EntityId[Creature],
    pos: Vector2,
    facingVector: Vector2 = Vector2(1, 0),
    damage: Float
) {}
