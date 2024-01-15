package com.mygdx.game.view

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.screen.SpriteBatch

trait Renderable {
  def pos(gameState: GameState): (Float, Float)

  def render(batch: SpriteBatch, gameState: GameState): Unit
}
