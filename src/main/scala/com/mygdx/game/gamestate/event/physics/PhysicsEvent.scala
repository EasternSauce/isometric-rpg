package com.mygdx.game.gamestate.event.physics

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.util.Vector2

sealed trait PhysicsEvent

case class TeleportEvent(creatureId: EntityId[Creature], pos: Vector2)
    extends PhysicsEvent

case class MakeBodySensorEvent(creatureId: EntityId[Creature])
    extends PhysicsEvent

case class MakeBodyNonSensorEvent(creatureId: EntityId[Creature])
    extends PhysicsEvent
