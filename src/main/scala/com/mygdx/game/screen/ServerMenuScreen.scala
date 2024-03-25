package com.mygdx.game.screen

import com.badlogic.gdx.scenes.scene2d.ui.{Skin, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, Screen}
import com.mygdx.game.core.CoreGame

case class ServerMenuScreen(game: CoreGame) extends Screen {
  var skin: Skin = _
  var stage: Stage = _

  override def show(): Unit = {
    skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"))
    stage = new Stage(new ScreenViewport())

    val button1: TextButton = new TextButton("Start server", skin, "default")
    button1.setX(Gdx.graphics.getWidth / 2 - 100)
    button1.setY(400)
    button1.setWidth(200)
    button1.setHeight(50)

    button1.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        game.setGameplayScreen()
      }
    })

    stage.addActor(button1)

    val button2: TextButton = new TextButton("Exit", skin, "default")
    button2.setX(Gdx.graphics.getWidth / 2 - 100)
    button2.setY(320)
    button2.setWidth(200)
    button2.setHeight(50)
    stage.addActor(button2)

    button2.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        System.exit(0)
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
