package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureSetDestinationEvent(
    creatureId: EntityId[Creature],
    destination: Vector2
) extends BroadcastEvent {

  override def applyToGameState(gameState: GameState): GameState = {
    if (gameState.creatures.contains(creatureId)) {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(
          _.modify(_.params.destination)
            .setTo(destination)
        )
      //        .modify(_.params.facingVector)
      //        .setToIf(velocity.length > 0)(velocity)
    } else {
      gameState
    }
  }
}
