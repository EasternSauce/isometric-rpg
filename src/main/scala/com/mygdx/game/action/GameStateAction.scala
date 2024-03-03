package com.mygdx.game.action

import com.mygdx.game.gamestate.GameState

trait GameStateAction {
  def applyToGameState(gameState: GameState): GameState
}
