package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.esotericsoftware.kryonet.{KryoSerialization, Server}
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.gamestate.{GameState, GameStateSideEffectsCollector}
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
    config.setForegroundFPS(60)
    config.setIdleFPS(60)
    new Lwjgl3Application(CoreGameServer, config)
  }

  override def onCreate(): Unit = {
    GameDataBroadcaster.start(server)
  }

  override def applySideEffectsToGameState(
      gameState: GameState,
      sideEffectsCollector: GameStateSideEffectsCollector
  ): GameState = {
    sendBroadcastEventsToAllConnectedClients(
      sideEffectsCollector.broadcastEvents
    )

    gameState
      .handleGameStateEvents(sideEffectsCollector.gameStateEvents)
      .handleCollisionEvents(sideEffectsCollector.collisionEvents)
      .handleBroadcastEvents(sideEffectsCollector.broadcastEvents)
  }

  private def sendBroadcastEventsToAllConnectedClients(
      broadcastEvents: List[BroadcastEvent]
  ): Unit = {
    server.getConnections.foreach(connection => {
      connection.sendTCP(BroadcastEventsHolder(broadcastEvents))
    })
  }

  private def applyBroadcastEventsToGameState(
      broadcastEvents: List[BroadcastEvent],
      gameState: GameState
  ): GameState = {
    broadcastEvents.foldLeft(gameState) {
      case (gameState: GameState, broadcastEvent: BroadcastEvent) =>
        broadcastEvent.applyToGameState(gameState)
    }
  }

}
