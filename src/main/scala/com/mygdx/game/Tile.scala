package com.mygdx.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.Tile.{convertIsometricCoordinates, textureMapping}
import com.mygdx.game.TileType.TileType

case class Tile(pos: TilePos, tileType: TileType) {

  def render(batch: SpriteBatch): Unit = {
    val (worldPosX, worldPosY) = convertIsometricCoordinates(pos.x, pos.y)
    batch.draw(
      Assets.atlas.get.findRegion(textureMapping(tileType)),
      worldPosX,
      worldPosY,
      Constants.TileTextureWidth,
      Constants.TileTextureHeight
    )
  }
}

object Tile {
  val textureMapping: Map[TileType, String] = {
    Map(TileType.Ground -> "ground", TileType.Tree -> "tree")
  }

  def convertIsometricCoordinates(x: Float, y: Float): (Float, Float) = {
    val worldPosX = ((x + 0.5f) - (y + 0.5f)) * Constants.TileSize / 2.0001f
    val worldPosY = ((x + 0.5f) + (y + 0.5f)) * (Constants.TileSize / 2f) / 2f
    (worldPosX, worldPosY)
  }
}
