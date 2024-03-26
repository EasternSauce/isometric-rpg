package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.core.CoreGameServer
import com.mygdx.game.input.Input

case class ServerGameplayScreen(game: CoreGameServer) extends Screen {

  override def show(): Unit = {
    new Thread(new Runnable() {
      override def run(): Unit = {
        game.runServer()
      }
    }).start()

    game.gameplay.init()
  }

  override def render(delta: Float): Unit = {
    val input = Input.poll()

    game.gameplay.update(input, delta)
    game.gameplay.render(input)
  }

  override def dispose(): Unit = {
    game.gameplay.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    game.gameplay.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
