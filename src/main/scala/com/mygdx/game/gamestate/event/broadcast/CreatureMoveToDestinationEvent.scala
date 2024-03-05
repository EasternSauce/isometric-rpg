package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureMoveToDestinationEvent(
    creatureId: EntityId[Creature],
    destination: Vector2
) extends BroadcastEvent {

  override def applyToGameState(gameState: GameState): GameState = {
    gameState
      .modify(_.creatures.at(creatureId).params.destination)
      .setTo(destination)

  }
}
