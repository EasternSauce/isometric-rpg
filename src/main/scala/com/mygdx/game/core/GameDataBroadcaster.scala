package com.mygdx.game.core

import com.esotericsoftware.kryonet.{Connection, Server}
import com.mygdx.game.Constants

object GameDataBroadcaster {
  private var broadcastThread: Thread = _

  def start(endPoint: Server): Unit = {
    broadcastThread = createBroadcastThread(endPoint)
    broadcastThread.start()
  }

  private def createBroadcastThread(endPoint: Server): Thread = new Thread {
    override def run(): Unit = {
      try while ({
        true
      }) {
        Thread.sleep(
          (Constants.TimeBetweenGameStateBroadcasts * 1000f).toInt
        )
        val connections = endPoint.getConnections
        for (connection <- connections) {
          broadcastToConnection(connection)
        }
      } catch {
        case e: InterruptedException =>

        // do nothing
      }
    }
  }

  def broadcastToConnection(connection: Connection): Unit = {
    connection.sendTCP(GameStateHolder(CoreGameServer.gameplay.gameState))
  }
}
