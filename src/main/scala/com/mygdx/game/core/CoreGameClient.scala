package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.{Client, KryoSerialization}
import com.mygdx.game.command.{ActionsPerformCommand, RegisterClientRequestCommand}
import com.mygdx.game.gamestate.event.broadcast.CreatureGoToEvent
import com.mygdx.game.gamestate.{GameState, GameStateSideEffectsCollector}
import com.mygdx.game.input.Input
import com.mygdx.game.screen.GameplayScreen
import com.mygdx.game.util.Vector2
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

case class CoreGameClient() extends CoreGame {
  var _clientId: Option[String] = None

  override protected val endPoint: Client = {
    val kryo: Kryo = {
      val instantiator = new ScalaKryoInstantiator
      instantiator.setRegistrationRequired(false)
      instantiator.newKryo()

    }
    new Client(8192 * 100, 2048 * 100, new KryoSerialization(kryo))
  }

  def client: Client = endPoint
  val listener: ClientListener = ClientListener(this)

  override val playScreen: Screen = GameplayScreen(gameplay, client)

  override def onCreate(): Unit = {
    endPoint.sendTCP(RegisterClientRequestCommand())
  }

  override def applySideEffectsToGameState(
      gameState: GameState,
      sideEffectsCollector: GameStateSideEffectsCollector
  ): GameState = {
    val newGameState = gameState
      .handleGameStateEvents(sideEffectsCollector.gameStateEvents)
      .handleCollisionEvents(sideEffectsCollector.collisionEvents)
      .handleBroadcastEvents(gameplay.scheduledBroadcastEvents)

    gameplay.clearScheduledBroadcastEvents()

    newGameState
  }

  def setClientId(clientId: String): Unit = {
    this._clientId = Some(clientId)
  }

  override protected def clientId: Option[String] = _clientId

  override def handleInput(input: Input): Unit = {
    val creature = clientCreature(gameplay.gameState)

    if (creature.isDefined && input.moveButtonPressed) {
      val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.get.pos)

      client.sendTCP(
        ActionsPerformCommand(
          List(CreatureGoToEvent(creature.get.id, mouseWorldPos))
        )
      )
    }
  }
}
