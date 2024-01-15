package com.mygdx.game.screen

import com.badlogic.gdx.graphics.g2d.{TextureRegion, SpriteBatch => GdxSpriteBatch}
import com.badlogic.gdx.math.Matrix4

case class SpriteBatch() {
  private var spriteBatch: GdxSpriteBatch = _

  def init(): Unit = {
    spriteBatch = new GdxSpriteBatch()
  }

  def begin(): Unit = spriteBatch.begin()

  def end(): Unit = spriteBatch.end()

  def draw(region: TextureRegion, x: Float, y: Float): Unit = {
    spriteBatch.draw(region, x, y)
  }

  def draw(
      region: TextureRegion,
      x: Float,
      y: Float,
      width: Int,
      height: Int
  ): Unit = {
    spriteBatch.draw(region, x, y, width, height)
  }

  def setProjectionMatrix(projection: Matrix4): Unit = {
    spriteBatch.setProjectionMatrix(projection)
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

}
