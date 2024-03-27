package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class PlayerToggleInventoryEvent(creatureId: EntityId[Creature])
    extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = {
    val playerState = gameState.playerStates(creatureId)

    gameState
      .modify(_.playerStates.at(creatureId).inventoryOpen)
      .setTo(!playerState.inventoryOpen)
  }
}
