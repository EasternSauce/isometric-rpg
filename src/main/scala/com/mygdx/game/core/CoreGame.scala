package com.mygdx.game.core

import com.badlogic.gdx.{Game, Screen}
import com.esotericsoftware.kryonet.EndPoint
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.{Assets, Gameplay}

abstract class CoreGame extends Game {
  val playScreen: Screen
  val endPoint: EndPoint

  private val _gameplay: Gameplay = Gameplay(this)

  override def create(): Unit = {
    Assets.load()

    setScreen(playScreen)

    onCreate()
  }

  def onCreate(): Unit

  def gameplay: Gameplay = _gameplay

  def onGameStateUpdate(broadcastEvents: List[BroadcastEvent]): Unit
}