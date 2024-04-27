package com.mygdx.game.core

import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive
import com.esotericsoftware.kryonet.{Connection, Listener}
import com.mygdx.game.command.{ActionsPerformCommand, RegisterClientRequestCommand, RegisterClientResponseCommand}
import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.gamestate.PlayerDisconnectEvent

case class ServerListener(game: CoreGameServer) extends Listener {
  override def disconnected(connection: Connection): Unit = {

    val reverseMap = for ((k, v) <- game.clientConnectionIds) yield (v, k)

    if (reverseMap.contains(connection.getID)) {
      val disconnectedCreatureId = reverseMap(connection.getID)
      val playerDisconnectEvent = PlayerDisconnectEvent(
        EntityId[Creature](disconnectedCreatureId)
      )
      game.gameplay.scheduleExternalEvent(List(playerDisconnectEvent))

      game.unregisterClient(disconnectedCreatureId, connection.getID)
    }
  }

  override def received(connection: Connection, obj: Any): Unit = {
    obj match {
      case RegisterClientRequestCommand(maybeClientId) =>
        val clientId = maybeClientId.getOrElse(game.generateNewClientId())
        game.registerClient(clientId, connection.getID)
        connection.sendTCP(RegisterClientResponseCommand(clientId))
      case ActionsPerformCommand(actions) =>
        game.gameplay.scheduleExternalEvent(actions)
      case _: KeepAlive =>
    }
  }
}
