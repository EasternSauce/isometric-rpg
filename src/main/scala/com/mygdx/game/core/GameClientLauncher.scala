package com.mygdx.game.core

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.mygdx.game.Constants

object GameClientLauncher {
  val game: CoreGameClient = CoreGameClient()

  def main(arg: Array[String]): Unit = {
    if (!Constants.OfflineMode) {
      game.client.start()
      game.client.connect(50000, "localhost", 54555, 54777)

      game.client.addListener(game.listener)
    }

    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.setForegroundFPS(120)
    config.setIdleFPS(120)
    new Lwjgl3Application(game, config)
  }
}