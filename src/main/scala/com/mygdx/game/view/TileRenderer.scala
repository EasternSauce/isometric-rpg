package com.mygdx.game.view

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.mygdx.game.gamestate.GameState

case class TileRenderer(cell: TiledMapTileLayer.Cell, col: Float, row: Float)
    extends Renderable {
  override def pos(gameState: GameState): (Float, Float) = (col, row)

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val textureRegion = cell.getTile.getTextureRegion
    val (x, y) =
      IsometricProjection.translateIsoToScreen(col + 0.75f, row - 0.85f)
    batch.draw(textureRegion, x, y)
  }

  def walkable: Boolean = {
    cell.getTile.getProperties.get("walkable").asInstanceOf[Boolean]
  }
}
