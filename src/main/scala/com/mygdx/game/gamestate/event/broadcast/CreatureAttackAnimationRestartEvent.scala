package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureAttackAnimationRestartEvent(
    creatureId: EntityId[Creature]
) extends BroadcastEvent {

  override def applyToGameState(gameState: GameState): GameState = {
    if (gameState.creatures.contains(creatureId)) {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(
          _.modify(_.params.attackAnimationTimer)
            .using(_.restart())
        )
    } else {
      gameState
    }
  }
}
