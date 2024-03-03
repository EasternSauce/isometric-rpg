package com.mygdx.game

import com.esotericsoftware.kryonet.{Connection, Server}

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
          (Constants.TIME_BETWEEN_GAMESTATE_BROADCASTS * 1000f).toInt
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
    connection.sendTCP(CoreGameServer.gameplay.currentGameState)
  }
}
