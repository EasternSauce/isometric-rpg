package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.Gameplay
import com.mygdx.game.input.Input

case class ServerCamScreen(gameplay: Gameplay) extends Screen {

  override def show(): Unit = {
    gameplay.init()
  }

  override def render(delta: Float): Unit = {
    val input = Input.poll()

    gameplay.update(input, delta)
    gameplay.render(input)
  }

  override def dispose(): Unit = {
    gameplay.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    gameplay.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
