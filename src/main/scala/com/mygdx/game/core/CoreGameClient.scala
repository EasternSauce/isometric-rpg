package com.mygdx.game.core

import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.{Client, KryoSerialization}
import com.mygdx.game.core.message.RegisterClientRequest
import com.mygdx.game.gamestate.{GameState, GameStateSideEffectsCollector}
import com.mygdx.game.screen.GameplayScreen
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

case class CoreGameClient() extends CoreGame {
  var _clientId: Option[String] = _

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
    endPoint.sendTCP(RegisterClientRequest())
  }

  override def applySideEffectsToGameState(
      gameState: GameState,
      sideEffectsCollector: GameStateSideEffectsCollector
  ): GameState = {
    val gs = gameState
      .handleGameStateEvents(sideEffectsCollector.gameStateEvents)
      .handleCollisionEvents(sideEffectsCollector.collisionEvents)
      .handleBroadcastEvents(gameplay.scheduledBroadcastEvents)

    gameplay.clearScheduledBroadcastEvents()

    gs
  }

  def setClientId(clientId: String): Unit = {
    this._clientId = Some(clientId)
  }

  override def clientId: Option[String] = _clientId
}
