package com.mygdx.game.core

import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive
import com.esotericsoftware.kryonet.{Connection, Listener}
import com.mygdx.game.command.{ActionsPerformCommand, RegisterClientResponseCommand}

case class ClientListener(game: CoreGameClient) extends Listener {

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case GameStateHolder(gameState) =>
        game.gameplay.overrideGameState(gameState)
      case ActionsPerformCommand(actions) =>
        game.gameplay.scheduleExternalEvent(actions)
      case RegisterClientResponseCommand(clientId) =>
        game.clientId = Some(clientId)
        game.clientRegistered = true
      case _: KeepAlive =>
    }
  }
}
