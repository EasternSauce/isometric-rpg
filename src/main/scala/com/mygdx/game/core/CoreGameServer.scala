package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.esotericsoftware.kryonet.{KryoSerialization, Server}
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.screen.ServerCamScreen
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

object CoreGameServer extends CoreGame {
  override val endPoint: Server = {
    val kryo: Kryo = {
      val instantiator = new ScalaKryoInstantiator
      instantiator.setRegistrationRequired(false)
      instantiator.newKryo()
    }

    new Server(16384 * 100, 2048 * 100, new KryoSerialization(kryo))
  }

  private def server: Server = endPoint
  private val listener: ServerListener = ServerListener(this)

  override val playScreen: Screen = ServerCamScreen(gameplay)

  private def runServer(): Unit = {
    server.start()
    server.bind(54555, 54777)

    server.addListener(listener)
  }

  def main(arg: Array[String]): Unit = {
    new Thread(new Runnable() {
      override def run(): Unit = {
        runServer()
      }
    }).start()

    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.useVsync(true)
    config.setForegroundFPS(60)
    new Lwjgl3Application(CoreGameServer, config)
  }

  override def onCreate(): Unit = {
    GameDataBroadcaster.start(server)
  }

  override def onGameStateUpdate(
      broadcastEvents: List[BroadcastEvent]
  ): Unit = {
    sendBroadcastEventsToAllConnectedClients(broadcastEvents)
  }

  private def sendBroadcastEventsToAllConnectedClients(
      broadcastEvents: List[BroadcastEvent]
  ): Unit = {
    server.getConnections.foreach(connection => {
      connection.sendTCP(BroadcastEventsHolder(broadcastEvents))
    })
  }
}
