package com.mygdx.game.core

import com.badlogic.gdx.{Game, Screen}
import com.esotericsoftware.kryonet.EndPoint
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.{Assets, Gameplay}

abstract class CoreGame extends Game {

  val menuScreen: Screen
  val gameplayScreen: Screen
  protected val endPoint: EndPoint

  private val _gameplay: Gameplay = Gameplay(this)

  override def create(): Unit = {
    Assets.load()

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

  def handleInput(input: Input): Unit

  def setGameplayScreen(): Unit = {
    setScreen(gameplayScreen)
  }
}
