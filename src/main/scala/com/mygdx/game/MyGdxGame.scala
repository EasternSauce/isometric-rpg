package com.mygdx.game

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.{Game, Screen}
import com.mygdx.game.screen.GameplayScreen

object MyGdxGame extends Game {

  private var playScreen: Screen = _

  override def create(): Unit = {
    Assets.load()

    playScreen = GameplayScreen
    setScreen(playScreen)
  }

  def main(arg: Array[String]): Unit = {
    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.useVsync(true)
    config.setForegroundFPS(60)
    new Lwjgl3Application(MyGdxGame, config)
  }
}
