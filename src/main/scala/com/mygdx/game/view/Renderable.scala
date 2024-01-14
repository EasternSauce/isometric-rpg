package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.gamestate.GameState

trait Renderable {
  def pos(gameState: GameState): (Float, Float)

  def render(batch: SpriteBatch, gameState: GameState): Unit
}
