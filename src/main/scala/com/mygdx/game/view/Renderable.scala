package com.mygdx.game.view

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.Vector2

trait Renderable {
  def pos(gameState: GameState): Vector2

  def alive(gameState: GameState): Boolean

  def render(batch: SpriteBatch, gameState: GameState): Unit
}
