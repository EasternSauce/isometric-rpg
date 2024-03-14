package com.mygdx.game.core

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.mygdx.game.Constants

object GameServerLauncher {
  val game: CoreGameServer = CoreGameServer()

  def main(arg: Array[String]): Unit = {
    new Thread(new Runnable() {
      override def run(): Unit = {
        game.runServer()
      }
    }).start()

    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.setForegroundFPS(60)
    config.setIdleFPS(60)
    new Lwjgl3Application(game, config)
  }
}
