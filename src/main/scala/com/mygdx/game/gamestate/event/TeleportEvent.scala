package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.util.Vector2

case class TeleportEvent(creatureId: EntityId[Creature], pos: Vector2)
    extends Event {}
