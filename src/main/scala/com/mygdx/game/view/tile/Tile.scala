package com.mygdx.game.view.tile

import com.badlogic.gdx.math.{Matrix4, Vector3}
import com.mygdx.game.Constants

object Tile {
  private val isoTransform: Matrix4 = {
    val matrix: Matrix4 = new Matrix4()

    matrix.idt()

    matrix.scale(
      (Math.sqrt(2.0) / 2.0).toFloat,
      (Math.sqrt(2.0) / 4.0).toFloat,
      1.0f
    )
    matrix.rotate(0.0f, 0.0f, 1.0f, -45)

    matrix
  }
  private val invIsoTransform = {
    new Matrix4(isoTransform).inv
  }

  def translateIsoToScreen(x: Float, y: Float): (Float, Float) = {
    val screenPos = new Vector3()
    screenPos.set(x, y, 0)
    screenPos.mul(isoTransform)
    (screenPos.x * Constants.TileSize, screenPos.y * Constants.TileSize)
  }

  def translateScreenToIso(x: Float, y: Float): (Float, Float) = {
    val screenPos = new Vector3()
    screenPos.set(x, y, 0)
    screenPos.mul(invIsoTransform)
    (screenPos.x * Constants.TileSize, screenPos.y * Constants.TileSize)
  }
}
