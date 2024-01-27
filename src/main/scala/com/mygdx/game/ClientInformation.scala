package com.mygdx.game

import com.mygdx.game.gamestate.{Creature, EntityId}

case class ClientInformation(clientCreatureId: EntityId[Creature]) {}
