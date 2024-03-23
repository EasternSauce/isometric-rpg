package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureRespawnDelayStartEvent(creatureId: EntityId[Creature])
    extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
    .modify(_.creatures.at(creatureId))
    .using(
      _.modify(_.params.respawnDelayTimer)
        .using(_.restart())
        .modify(_.params.respawnDelayInProgress)
        .setTo(true)
    )
}
