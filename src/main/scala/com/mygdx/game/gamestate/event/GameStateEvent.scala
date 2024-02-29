package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.util.Vector2

sealed trait GameStateEvent extends Event

case class CreatureDeathEvent(creatureId: EntityId[Creature])
    extends GameStateEvent

case class CreatureRespawnDelayStartEvent(creatureId: EntityId[Creature])
    extends GameStateEvent

case class CreatureRespawnEvent(creatureId: EntityId[Creature])
    extends GameStateEvent

case class MeleeAttackHitsCreatureEvent(
    sourceCreatureId: EntityId[Creature],
    destinationCreatureId: EntityId[Creature],
    damage: Float
) extends GameStateEvent

case class CreatureShootArrowEvent(
    sourceCreatureId: EntityId[Creature],
    arrowDirection: Vector2,
    damage: Float
) extends GameStateEvent
