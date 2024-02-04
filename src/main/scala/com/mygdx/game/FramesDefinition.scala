package com.mygdx.game

case class FramesDefinition(start: Int, count: Int, frameDuration: Float) {
  def totalDuration: Float = frameDuration * count
}
