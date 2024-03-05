package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureRespawnEvent(creatureId: EntityId[Creature])
    extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
    .modify(_.creatures.at(creatureId))
    .using(creature =>
      creature
        .modify(_.params.life)
        .setTo(creature.params.maxLife)
        .modify(_.params.deathAcknowledged)
        .setTo(false)
        .modify(_.params.respawnTimer)
        .using(_.stop())
    )
}
