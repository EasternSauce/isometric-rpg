package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.ModifyPimp

case class PlayerDisconnectEvent(creatureId: EntityId[Creature])
    extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = {
    gameState.modify(_.activeCreatureIds).using(_ - creatureId)
  }
}
