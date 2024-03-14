package com.mygdx.game.core

import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive
import com.esotericsoftware.kryonet.{Connection, Listener}
import com.mygdx.game.core.message.{RegisterClientRequest, RegisterClientResponse}

case class ServerListener(game: CoreGameServer) extends Listener {
  override def disconnected(connection: Connection): Unit = {
    System.out.println("Disconnecting...")
    System.exit(0)
  }

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case RegisterClientRequest() =>
        val clientId = game.generateNewClientId()
        game.registerClient(clientId, connection.getID)
        connection.sendTCP(RegisterClientResponse(clientId))
      case _: KeepAlive =>
    }
  }
}
