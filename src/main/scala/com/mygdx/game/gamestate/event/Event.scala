package com.mygdx.game.gamestate.event

import com.mygdx.game.gamestate.GameState

trait Event {
  def applyToGameState(gameState: GameState): GameState
}
