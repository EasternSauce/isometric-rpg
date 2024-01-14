package com.mygdx.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.Tile.{convertIsometricCoordinates, textureMapping}
import com.mygdx.game.TileType.TileType

case class Tile(tilePos: TilePos, tileType: TileType) extends Renderable {

  override def pos(gameState: GameState): (Float, Float) = {
    convertIsometricCoordinates(tilePos.x, tilePos.y)
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val (worldPosX, worldPosY) =
      convertIsometricCoordinates(tilePos.x, tilePos.y)
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
    val worldPosX = (x - y) * Constants.TileSize / 2.0001f
    val worldPosY = (x + y) * (Constants.TileSize / 2f) / 2f
    (worldPosX, worldPosY)
  }
}
