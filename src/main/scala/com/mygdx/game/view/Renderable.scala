package com.mygdx.game.view

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.util.Vector2

trait Renderable {
  def pos(gameState: GameState): Vector2

  def areaId(gameState: GameState): AreaId

  def renderPriority(gameState: GameState): Boolean

  def render(
      batch: SpriteBatch,
      worldCameraPos: Vector2,
      gameState: GameState
  ): Unit
}
