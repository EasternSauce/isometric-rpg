package com.mygdx.game.screen

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, Screen}
import com.mygdx.game.core.CoreGameServer

case class ServerMenuScreen(game: CoreGameServer) extends Screen {

  var stage: Stage = _

  override def show(): Unit = {
    stage = new Stage(new ScreenViewport())

    val startButton: TextButton =
      new TextButton("Start server", game.scene2dSkin, "default")
    startButton.setX(Gdx.graphics.getWidth / 2 - 100)
    startButton.setY(400)
    startButton.setWidth(200)
    startButton.setHeight(50)

    startButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        game.setGameplayScreen()
      }
    })

    stage.addActor(startButton)

    val exitButton: TextButton =
      new TextButton("Exit", game.scene2dSkin, "default")
    exitButton.setX(Gdx.graphics.getWidth / 2 - 100)
    exitButton.setY(320)
    exitButton.setWidth(200)
    exitButton.setHeight(50)
    stage.addActor(exitButton)

    exitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        Gdx.app.exit()
      }
    })

    Gdx.input.setInputProcessor(stage)

  }

  override def render(delta: Float): Unit = {
    ScreenUtils.clear(0, 0, 0.2f, 1)

    stage.act(delta)
    stage.draw()
  }

  override def resize(width: Int, height: Int): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {}
}
