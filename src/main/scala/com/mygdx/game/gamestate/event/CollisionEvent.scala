package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature

sealed trait CollisionEvent extends Event

case class AbilityHitsCreatureEvent(
    abilityId: EntityId[Ability],
    creatureId: EntityId[Creature]
) extends CollisionEvent

case class AbilityHitsTerrainEvent(
    abilityId: EntityId[Ability],
    terrainId: String
) extends CollisionEvent
