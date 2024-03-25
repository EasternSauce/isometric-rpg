package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureGoToEvent(
    creatureId: EntityId[Creature],
    destination: Vector2
) extends GameStateEvent {

  override def applyToGameState(gameState: GameState): GameState = {
    if (
      gameState.creatures
        .contains(creatureId) && gameState.creatures(creatureId).alive
    ) {
      val creature = gameState.creatures(creatureId)

      val vectorTowardsDestination =
        creature.pos.vectorTowards(creature.params.destination)

      gameState
        .modify(_.creatures.at(creatureId))
        .using(
          _.modify(_.params.destination)
            .setTo(destination)
        )
        .modify(_.creatures.at(creatureId).params.facingVector)
        .setToIf(vectorTowardsDestination.length > 0)(vectorTowardsDestination)
    } else {
      gameState
    }
  }
}
