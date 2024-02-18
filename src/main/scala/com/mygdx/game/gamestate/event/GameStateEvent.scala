package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature

sealed trait GameStateEvent extends Event

case class CreatureDeathEvent(creatureId: EntityId[Creature])
    extends GameStateEvent

case class CreatureRespawnEvent(creatureId: EntityId[Creature])
    extends GameStateEvent

case class CreatureAttackEvent(
    sourceCreatureId: EntityId[Creature],
    destinationCreatureId: EntityId[Creature],
    damage: Float
) extends GameStateEvent
