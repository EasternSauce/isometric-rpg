package com.mygdx.game.view.tile

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.screen.SpriteBatch
import com.mygdx.game.view.Renderable

case class Cell(cell: TiledMapTileLayer.Cell, col: Float, row: Float)
    extends Renderable {
  override def pos(gameState: GameState): (Float, Float) = (col, row)

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val textureRegion = cell.getTile.getTextureRegion
    val (x, y) = Tile.translateIsoToScreen(col + 0.5f, row + 0.5f)
    batch.draw(textureRegion, x, y)
  }

  def walkable: Boolean = {
    cell.getTile.getProperties.get("walkable").asInstanceOf[Boolean]
  }
}
