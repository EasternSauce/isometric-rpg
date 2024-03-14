package com.mygdx.game.core

import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive
import com.esotericsoftware.kryonet.{Connection, Listener}
import com.mygdx.game.core.message.RegisterClientResponse

case class ClientListener(game: CoreGameClient) extends Listener {
  override def disconnected(connection: Connection): Unit = {
    System.out.println("Disconnecting...")
    System.exit(0)
  }

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case GameStateHolder(gameState) =>
        game.gameplay.overrideGameState(gameState)
      case BroadcastEventsHolder(broadcastEvents) =>
        game.gameplay.scheduleBroadcastEvents(broadcastEvents)
      case RegisterClientResponse(clientId) =>
        game.setClientId(clientId)
      case _: KeepAlive =>
    }
  }
}
