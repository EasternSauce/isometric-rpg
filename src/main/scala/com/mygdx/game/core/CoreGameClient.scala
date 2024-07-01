package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.{Client, KryoSerialization}
import com.mygdx.game.Constants
import com.mygdx.game.command.ActionsPerformCommand
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.event.gamestate.{CreatureAttackEvent, CreatureGoToEvent, PlayerToggleInventoryEvent}
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.screen.{ClientGameplayScreen, ClientMenuScreen}
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

case class CoreGameClient() extends CoreGame {
  var host: Option[String] = None
  var port: Option[String] = None

  var clientId: Option[String] = None
  var clientRegistered = false

  override protected val endPoint: Client = {
    if (!Constants.OfflineMode) {
      val kryo: Kryo = {
        val instantiator = new ScalaKryoInstantiator
        instantiator.setRegistrationRequired(false)
        instantiator.newKryo()

      }
      new Client(8192 * 100, 2048 * 100, new KryoSerialization(kryo))
    } else {
      null
    }
  }

  def client: Client = endPoint
  val listener: ClientListener = ClientListener(this)

  override val menuScreen: Screen = ClientMenuScreen(this)
  override val gameplayScreen: Screen = ClientGameplayScreen(this)

  override def onCreate(): Unit = {}

  override def applyOutcomeEvents(
      gameStateOutcome: Outcome[GameState]
  ): GameState = {
    gameplay.physics.scheduleEvents(gameStateOutcome.physicsEvents)

    val newGameState = gameStateOutcome.obj
      .handleGameStateEvents(
        gameplay.physics.pollCollisionEvents()
          ++ gameStateOutcome.gameStateEvents
          ++ gameplay.externalEvents
      )
      .pipeIf(_ => Constants.OfflineMode)(
        _.handleGameStateEvents(gameStateOutcome.broadcastEvents)
      )

    gameplay.clearExternalEventsQueue()

    newGameState
  }

  override def sendEvent(event: GameStateEvent): Unit = {
    if (!Constants.OfflineMode) {
      client.sendTCP(
        ActionsPerformCommand(
          List(event)
        )
      )
    } else {
      gameplay.scheduleExternalEvent(
        List(event)
      )
    }
  }

  override def handleInput(input: Input): Unit = {
    val creature = clientCreature(gameplay.gameState)

    val inventoryOpen =
      clientCreatureId.isDefined &&
        gameplay.gameState.playerStates.contains(clientCreatureId.get) &&
        gameplay.gameState.playerStates(clientCreatureId.get).inventoryOpen

    if (creature.isDefined) {
      if (!inventoryOpen) {
        if (input.moveButtonPressed) {
          val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.get.pos)

          sendEvent(CreatureGoToEvent(creature.get.id, mouseWorldPos))
        }
        if (input.attackButtonJustPressed) {
          val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.get.pos)

          sendEvent(CreatureAttackEvent(creature.get.id, mouseWorldPos))
        }
      }
      if (input.inventoryToggleKeyJustPressed) {
        sendEvent(PlayerToggleInventoryEvent(creature.get.id))
      }
    }
  }

  override def dispose(): Unit = {
    super.dispose()
    if (client != null) {
      client.close()
    }
  }

  override def updatePhysics(): Unit = {
    gameplay.physics.updateForArea(
      clientAreaId(gameplay.gameState).getOrElse(Constants.defaultAreaId),
      gameState
    )
  }
}
