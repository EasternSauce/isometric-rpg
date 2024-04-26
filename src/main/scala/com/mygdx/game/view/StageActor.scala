package com.mygdx.game.view

import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}

abstract class StageActor {
  protected var _actor: Actor = _

  def addToStage(stage: Stage): Unit = {
    stage.addActor(_actor)
  }

  def scene2dActor: Actor = _actor
}
