package com.mygdx.game

import com.esotericsoftware.kryonet.{Connection, Listener}
import com.mygdx.game.gamestate.GameState

case class ServerListener(game: CoreGame) extends Listener {
  override def disconnected(connection: Connection): Unit = {
    System.out.println("Disconnecting...")
    System.exit(0)
  }

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case gameState: GameState => ???
      case _                    =>
    }
  }
}
