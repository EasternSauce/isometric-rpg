package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{TextureRegion, SpriteBatch => GdxSpriteBatch}
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.{Matrix4, Rectangle => GdxRectangle}
import com.mygdx.game.util.Rectangle
import space.earlygrey.shapedrawer.ShapeDrawer

case class SpriteBatch() {
  private var spriteBatch: GdxSpriteBatch = _
  private var shapeDrawer: ShapeDrawer = _

  private var texture: Texture = _

  def init(): Unit = {
    spriteBatch = new GdxSpriteBatch()
    shapeDrawer = new ShapeDrawer(spriteBatch, createTextureAndRegion())
  }

  private def createTextureAndRegion(): TextureRegion = {
    val pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.WHITE)
    pixmap.drawPixel(0, 0)
    texture = new Texture(pixmap) //remember to dispose of later

    pixmap.dispose()
    new TextureRegion(texture, 0, 0, 1, 1)
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

  def filledRectangle(rect: Rectangle, color: Color): Unit = {
    shapeDrawer.filledRectangle(
      new GdxRectangle(rect.x, rect.y, rect.width, rect.height),
      color
    )
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
    texture.dispose()
  }

}
