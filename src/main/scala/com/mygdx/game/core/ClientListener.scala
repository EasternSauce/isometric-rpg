package com.mygdx.game.core

import com.esotericsoftware.kryonet.{Connection, Listener}

case class ClientListener(game: CoreGame) extends Listener {
  override def disconnected(connection: Connection): Unit = {
    System.out.println("Disconnecting...")
    System.exit(0)
  }

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case GameStateHolder(gameState) =>
        game.gameplay.overrideGameState(gameState)
      case BroadcastEventsHolder(broadcastEvents) =>
        game.gameplay.applyBroadcastEvents(broadcastEvents)
    }
  }
}
