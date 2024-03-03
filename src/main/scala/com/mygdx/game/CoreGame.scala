package com.mygdx.game

import com.badlogic.gdx.{Game, Screen}
import com.esotericsoftware.kryonet.EndPoint

abstract class CoreGame extends Game {

  val playScreen: Screen
  val endPoint: EndPoint

  val gameplay: Gameplay = Gameplay()

  override def create(): Unit = {
    Assets.load()

    setScreen(playScreen)

    onCreate()
  }

  def onCreate(): Unit
}
