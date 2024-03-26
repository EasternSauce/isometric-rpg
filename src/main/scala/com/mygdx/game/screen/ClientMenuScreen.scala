package com.mygdx.game.screen

import com.badlogic.gdx.scenes.scene2d.ui.{TextButton, TextField}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, Screen}
import com.mygdx.game.core.CoreGameClient

case class ClientMenuScreen(game: CoreGameClient) extends Screen {

  var stage: Stage = _

  override def show(): Unit = {
    stage = new Stage(new ScreenViewport())

    val nameField = new TextField("", game.skin)
    nameField.setMessageText("name")
    nameField.setX(Gdx.graphics.getWidth / 2 - 100)
    nameField.setY(400)
    nameField.setWidth(200)
    nameField.setHeight(50)
    stage.addActor(nameField)

    val hostField = new TextField("", game.skin)
    hostField.setMessageText("host")
    hostField.setX(Gdx.graphics.getWidth / 2 - 100)
    hostField.setY(320)
    hostField.setWidth(125)
    hostField.setHeight(50)
    stage.addActor(hostField)

    val portField = new TextField("", game.skin)
    portField.setMessageText("port")
    portField.setX(Gdx.graphics.getWidth / 2 + 25)
    portField.setY(320)
    portField.setWidth(75)
    portField.setHeight(50)
    stage.addActor(portField)

    val joinButton: TextButton =
      new TextButton("Join game", game.skin, "default")
    joinButton.setX(Gdx.graphics.getWidth / 2 - 100)
    joinButton.setY(240)
    joinButton.setWidth(200)
    joinButton.setHeight(50)

    joinButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        if (nameField.getText.nonEmpty) {
          game.clientId = Some(nameField.getText)
        }

        if (hostField.getText.nonEmpty) {
          game.host = Some(hostField.getText)
        }

        if (portField.getText.nonEmpty) {
          game.port = Some(portField.getText)
        }

        game.setGameplayScreen()
      }
    })

    stage.addActor(joinButton)

    val exitButton: TextButton = new TextButton("Exit", game.skin, "default")
    exitButton.setX(Gdx.graphics.getWidth / 2 - 100)
    exitButton.setY(160)
    exitButton.setWidth(200)
    exitButton.setHeight(50)
    stage.addActor(exitButton)

    exitButton.addListener(new ClickListener() {
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
