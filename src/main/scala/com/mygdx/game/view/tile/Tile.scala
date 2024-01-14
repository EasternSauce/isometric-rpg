package com.mygdx.game.view.tile

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.view.Renderable
import com.mygdx.game.view.tile.Tile.{convertToIsometricCoordinates, textureMapping}
import com.mygdx.game.view.tile.TileType.TileType
import com.mygdx.game.{Assets, Constants}

case class Tile(x: Int, y: Int, tileType: TileType) extends Renderable {

  override def pos(gameState: GameState): (Float, Float) = {
    convertToIsometricCoordinates(x, y)
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val (worldPosX, worldPosY) =
      convertToIsometricCoordinates(x, y)
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

  def convertToIsometricCoordinates(x: Float, y: Float): (Float, Float) = {
    val worldPosX = (x - y) * Constants.TileSize / 2.0001f
    val worldPosY = (x + y) * (Constants.TileSize / 2f) / 2f
    (worldPosX, worldPosY)
  }
}
