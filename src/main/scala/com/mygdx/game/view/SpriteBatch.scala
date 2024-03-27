package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureRegion, SpriteBatch => GdxSpriteBatch}
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.{Matrix4, Rectangle => GdxRectangle}
import com.mygdx.game.util.{Rectangle, Vector2}
import space.earlygrey.shapedrawer.ShapeDrawer

case class SpriteBatch() {

  private var _spriteBatch: GdxSpriteBatch = _
  private var shapeDrawer: ShapeDrawer = _

  private var texture: Texture = _

  def init(): Unit = {
    _spriteBatch = new GdxSpriteBatch()
    shapeDrawer = new ShapeDrawer(_spriteBatch, createTextureAndRegion())
  }

  private def createTextureAndRegion(): TextureRegion = {
    val pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.WHITE)
    pixmap.drawPixel(0, 0)
    texture = new Texture(pixmap) //remember to dispose of later

    pixmap.dispose()
    new TextureRegion(texture, 0, 0, 1, 1)
  }

  def begin(): Unit = _spriteBatch.begin()

  def end(): Unit = _spriteBatch.end()

  def draw(region: TextureRegion, x: Float, y: Float): Unit = {
    _spriteBatch.draw(region, x, y)
  }

  def draw(
      region: TextureRegion,
      x: Float,
      y: Float,
      width: Int,
      height: Int,
      rotation: Float = 0f
  ): Unit = {
    _spriteBatch.draw(
      region,
      x,
      y,
      width / 2f,
      height / 2f,
      width,
      height,
      1f,
      1f,
      rotation
    )
  }

  def setProjectionMatrix(projection: Matrix4): Unit = {
    _spriteBatch.setProjectionMatrix(projection)
  }

  def filledRectangle(rect: Rectangle, color: Color): Unit = {
    shapeDrawer.filledRectangle(
      new GdxRectangle(rect.x, rect.y, rect.width, rect.height),
      color
    )
  }

  def drawFont(font: BitmapFont, str: String, pos: Vector2): Unit = {
    font.draw(_spriteBatch, str, pos.x, pos.y)

  }

  def spriteBatch: GdxSpriteBatch = _spriteBatch

  def dispose(): Unit = {
    _spriteBatch.dispose()
    texture.dispose()
  }

}
