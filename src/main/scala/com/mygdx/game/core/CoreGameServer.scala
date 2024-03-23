package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.{KryoSerialization, Server}
import com.mygdx.game.command.ActionsPerformCommand
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.screen.ServerCamScreen
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

case class CoreGameServer() extends CoreGame {

  override protected val endPoint: Server = {
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

  private val gameDataBroadcaster: GameDataBroadcaster = GameDataBroadcaster(
    this
  )

  private var clientCounter = 0

  private var clientConnectionIds: Map[String, Int] = Map()

  def runServer(): Unit = {
    server.start()
    server.bind(54555, 54777)

    server.addListener(listener)
  }

  override def onCreate(): Unit = {
    gameDataBroadcaster.start(server)
  }

  override def applyOutcomeEvents(
      gameStateOutcome: Outcome[GameState]
  ): GameState = {
    gameplay.physics.scheduleEvents(gameStateOutcome.physicsEvents)

    sendBroadcastEventsToAllConnectedClients(
      gameStateOutcome.broadcastEvents ++ gameplay.externalEvents
    )

    val newGameState = gameStateOutcome.obj
      .handleGameStateEvents(
        gameplay.physics.pollCollisionEvents() ++
          gameStateOutcome.gameStateEvents ++
          gameStateOutcome.broadcastEvents ++
          gameplay.externalEvents
      )

    gameplay.clearExternalEventsQueue()

    newGameState
  }

  private def sendBroadcastEventsToAllConnectedClients(
      broadcastEvents: List[GameStateEvent]
  ): Unit = {
    server.getConnections.foreach(connection => {
      if (
        broadcastEvents.nonEmpty && clientConnectionIds.values.toSet
          .contains(connection.getID)
      ) {
        connection.sendTCP(ActionsPerformCommand(broadcastEvents))
      }
    })
  }

  def generateNewClientId(): String = {
    val id = "client" + clientCounter

    clientCounter = clientCounter + 1

    id
  }

  def registerClient(clientId: String, connectionId: Int): Unit = {
    clientConnectionIds = clientConnectionIds.updated(clientId, connectionId)

    gameplay.schedulePlayerCreaturesToCreate(clientId)
  }

  override protected def clientId: Option[String] = None

  override def handleInput(input: Input): Unit = {}
}
