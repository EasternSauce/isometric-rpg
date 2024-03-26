package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.Constants
import com.mygdx.game.command.RegisterClientRequestCommand
import com.mygdx.game.core.CoreGameClient
import com.mygdx.game.input.Input

case class ClientGameplayScreen(game: CoreGameClient) extends Screen {

  override def show(): Unit = {
    if (!Constants.OfflineMode) {
      game.client.start()
      game.client.connect(
        50000,
        game.host.getOrElse("localhost"),
        game.port.map(_.toInt).getOrElse(54555),
        54777
      )

      game.client.addListener(game.listener)
    }

    game.gameplay.init()

    if (!Constants.OfflineMode) {
      game.client.sendTCP(RegisterClientRequestCommand(game.clientId))
    } else {
      val clientId = "offline_client"

      game.clientId = Some(clientId)
      game.clientRegistered = true

      game.gameplay.schedulePlayerCreaturesToCreate(clientId)
    }
  }

  override def render(delta: Float): Unit = {
    val input = Input.poll()

    game.gameplay.update(input, delta)
    game.gameplay.render(input)
  }

  override def dispose(): Unit = {
    game.gameplay.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    game.gameplay.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
