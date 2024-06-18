package com.mygdx.game.view

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.Vector2

case class Cell(tiledCell: TiledMapTileLayer.Cell, pos: Vector2)
    extends Renderable {
  override def pos(gameState: GameState): Vector2 = pos

  override def render(
      batch: SpriteBatch,
      worldCameraPos: Vector2,
      gameState: GameState
  ): Unit = {
    val textureRegion = tiledCell.getTile.getTextureRegion
    val textureWidth = textureRegion.getRegionWidth
    val textureHeight = textureRegion.getRegionHeight

    val screenPos =
      IsometricProjection.translatePosIsoToScreen(
        Vector2(pos.x + 0.75f, pos.y - 0.85f)
      )

    if (worldCameraPos.distance(screenPos) < 1000f) {
      batch.draw(
        textureRegion,
        screenPos.x + Constants.TileCenterX + tiledCell.getTile.getOffsetX,
        screenPos.y + Constants.TileCenterY + tiledCell.getTile.getOffsetY,
        (textureWidth * Constants.MapTextureScale).toInt,
        (textureHeight * Constants.MapTextureScale).toInt
      )
    }
  }

  def walkable: Boolean = {
    tiledCell.getTile.getProperties.get("walkable").asInstanceOf[Boolean]
  }

  override def renderPriority(gameState: GameState): Boolean = false
}
