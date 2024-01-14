package com.mygdx.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch

trait Renderable {
  def pos(gameState: GameState): (Float, Float)

  def render(batch: SpriteBatch, gameState: GameState): Unit
}
