package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.{Client, KryoSerialization}
import com.mygdx.game.Constants
import com.mygdx.game.command.{ActionsPerformCommand, RegisterClientRequestCommand}
import com.mygdx.game.gamestate.event.broadcast.{CreatureAttackEvent, CreatureGoToEvent}
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.screen.GameplayScreen
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

case class CoreGameClient() extends CoreGame {
  var _clientId: Option[String] = None

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

  override val playScreen: Screen = GameplayScreen(gameplay, client)

  override def onCreate(): Unit = {
    if (!Constants.OfflineMode) {
      endPoint.sendTCP(RegisterClientRequestCommand())
    } else {
      val clientId = "offline_client"

      setClientId(clientId)

      gameplay.schedulePlayerCreaturesToCreate(clientId)
    }
  }

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

  def setClientId(clientId: String): Unit = {
    this._clientId = Some(clientId)
  }

  override protected def clientId: Option[String] = _clientId

  override def handleInput(input: Input): Unit = {
    val creature = clientCreature(gameplay.gameState)

    if (creature.isDefined) {
      if (input.moveButtonPressed) {
        val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.get.pos)

        if (!Constants.OfflineMode) {
          client.sendTCP(
            ActionsPerformCommand(
              List(CreatureGoToEvent(creature.get.id, mouseWorldPos))
            )
          )
        } else {
          gameplay.scheduleExternalEvent(
            List(CreatureGoToEvent(creature.get.id, mouseWorldPos))
          )
        }
      }
      if (input.attackButtonJustPressed) {
        val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.get.pos)

        if (!Constants.OfflineMode) {
          client.sendTCP(
            ActionsPerformCommand(
              List(CreatureAttackEvent(creature.get.id, mouseWorldPos))
            )
          )
        } else {
          gameplay.scheduleExternalEvent(
            List(CreatureAttackEvent(creature.get.id, mouseWorldPos))
          )
        }
      }
    }
  }
}
