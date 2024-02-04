package com.mygdx.game

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature

case class ClientInformation(clientCreatureId: EntityId[Creature]) {}
