package com.mygdx.game.view

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Window

abstract class StageActor {
  protected var _actor: Actor = _

  def addToWindow(window: Window): Unit = {
    window.addActor(_actor)
  }
}
