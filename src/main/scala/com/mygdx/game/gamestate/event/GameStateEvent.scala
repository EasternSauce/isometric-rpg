package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.GameState

trait GameStateEvent {
  def applyToGameState(gameState: GameState): GameState
}
