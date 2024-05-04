package com.mygdx.game.core

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.{Game, Gdx, Screen}
import com.esotericsoftware.kryonet.EndPoint
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.playerstate.PlayerState
import com.mygdx.game.gamestate.{EntityId, GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.mygdx.game.{Assets, Gameplay}

abstract class CoreGame extends Game {

  val menuScreen: Screen
  val gameplayScreen: Screen
  protected val endPoint: EndPoint

  private val _gameplay: Gameplay = Gameplay(this)

  var _skin: Skin = _

  override def create(): Unit = {
    Assets.load()

    _skin = new Skin(Gdx.files.internal("assets/ui/uiskin.json"))

    setScreen(menuScreen)

    onCreate()
  }

  def onCreate(): Unit

  def gameplay: Gameplay = _gameplay

  def applyOutcomeEvents(gameStateOutcome: Outcome[GameState]): GameState

  protected def clientId: Option[String]

  def clientCreature(gameState: GameState): Option[Creature] = {
    clientCreatureId
      .filter(creatureId => gameState.creatures.contains(creatureId))
      .map(creatureId => gameState.creatures(creatureId))
  }

  def clientCreatureId: Option[EntityId[Creature]] = {
    clientId.map(EntityId[Creature])
  }

  def clientPlayerState(gameState: GameState): Option[PlayerState] = {
    for {
      clientCreatureId <- clientCreatureId
      playerState <- gameState.playerStates.get(clientCreatureId)
    } yield playerState
  }

  def handleInput(input: Input): Unit

  def setGameplayScreen(): Unit = {
    setScreen(gameplayScreen)
  }

  def sendEvent(event: GameStateEvent): Unit

  def scene2dSkin: Skin = _skin

  def mousePos(): Vector2 = {
    val screenCoords =
      new Vector3(Gdx.input.getX.toFloat, Gdx.input.getY.toFloat, 0f)
    gameplay.view.unprojectHudCamera(screenCoords)
    Vector2(screenCoords.x, screenCoords.y)
  }

  def gameState: GameState = gameplay.gameState
}
