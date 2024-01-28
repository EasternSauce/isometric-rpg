package com.mygdx.game.util

case class Vector2(x: Float, y: Float) {
  def midpointTo(vector: Vector2): Vector2 = Vector2(
    this.x + 0.5f * (vector.x - this.x),
    this.y + 0.5f * (vector.y - this.y)
  )

  def add(vector: Vector2): Vector2 = Vector2(x + vector.x, y + vector.y)

  def distance(v: Vector2): Float = {
    val x_d = v.x - x
    val y_d = v.y - y
    Math.sqrt(x_d * x_d + y_d * y_d).toFloat
  }

  def angleDeg: Float = {
    var angle = Math.atan2(y, x).toFloat * 180f / 3.141592653589793f
    if (angle < 0) angle += 360
    angle
  }

  def withLength(length: Float): Vector2 = {
    normalized.multiply(length)
  }

  def normalized: Vector2 = {
    if (length != 0) {
      Vector2(x / length, y / length)
    } else {
      Vector2(x, y)
    }
  }

  def length: Float = Math.sqrt(x * x + y * y).toFloat

  def multiply(value: Float): Vector2 = Vector2(x * value, y * value)

  def vectorTowards(point: Vector2): Vector2 = {
    Vector2(point.x - x, point.y - y)
  }
}
