package com.mygdx.game.view

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState

case class TileRenderer(cell: TiledMapTileLayer.Cell, col: Float, row: Float)
    extends Renderable {
  override def pos(gameState: GameState): (Float, Float) = (col, row)

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val textureRegion = cell.getTile.getTextureRegion
    val textureWidth = textureRegion.getRegionWidth
    val textureHeight = textureRegion.getRegionHeight

    val (x, y) =
      IsometricProjection.translateIsoToScreen(col + 0.75f, row - 0.85f)
    batch.draw(
      textureRegion,
      x,
      y,
      (textureWidth * Constants.MapTextureScale).toInt,
      (textureHeight * Constants.MapTextureScale).toInt
    )
  }

  def walkable: Boolean = {
    cell.getTile.getProperties.get("walkable").asInstanceOf[Boolean]
  }
}
