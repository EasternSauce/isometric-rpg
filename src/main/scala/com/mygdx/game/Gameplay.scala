package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.input.Input
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.view.{IsometricProjection, SpriteBatch, View}

case class Gameplay() {

  private val clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = EntityId("player"))

  private val levelMap: LevelMap = LevelMap()
  private val physics: Physics = Physics()
  private val view: View = View()
  private val spriteBatch: SpriteBatch = SpriteBatch()
  private val modelEventsScheduler: ModelEventsScheduler =
    ModelEventsScheduler()
  private var gameState: GameState = _

  def init(): Unit = {
    gameState = GameState.initialState(clientInformation)

    levelMap.init()
    view.init(clientInformation, levelMap, gameState)
    physics.init(clientInformation, levelMap, gameState)

    spriteBatch.init()
  }

  def update(input: Input, delta: Float): Unit = {
    physics.update(gameState)

    updateGameState(
      physics.getCreaturePositions,
      physics.getAbilityPositions,
      input,
      delta
    )

    processModelEvents(gameState)
  }

  def render(input: Input): Unit = {
    view.update(clientInformation, gameState)

    view.draw(spriteBatch, physics, gameState)

    if (Constants.EnableDebug) drawMouseAimDebug(input)
  }

  private def drawMouseAimDebug(input: Input): Unit = {
    val mousePos = input.mousePos

    val isoMousePos =
      IsometricProjection.translatePosScreenToIso(mousePos)

    val mouseWorldPos = gameState
      .creatures(clientInformation.clientCreatureId)
      .params
      .pos
      .add(isoMousePos)

    spriteBatch.begin()

    gameState.creatures.values
      .map(_.pos)
      .foreach(pos => {
        val worldPos = IsometricProjection.translatePosIsoToScreen(pos)
        spriteBatch.filledRectangle(
          Rectangle(worldPos.x - 5, worldPos.y - 5, 10, 10),
          Color.CYAN
        )
      })

    val mouseScreenPos =
      IsometricProjection.translatePosIsoToScreen(mouseWorldPos)

    spriteBatch.filledRectangle(
      Rectangle(mouseScreenPos.x - 5, mouseScreenPos.y - 5, 10, 10),
      Color.CYAN
    )
    spriteBatch.end()
  }

  private def updateGameState(
      creaturePositions: Map[EntityId[Creature], Vector2],
      abilityPositions: Map[EntityId[Ability], Vector2],
      input: Input,
      delta: Float
  ): Unit = {
    val newGameState = gameState.update(
      creaturePositions,
      abilityPositions,
      input,
      clientInformation,
      physics,
      delta
    )

    scheduleModelEvents(gameState, newGameState)

    gameState = newGameState
  }

  def scheduleModelEvents(
      oldGameState: GameState,
      newGameState: GameState
  ): Unit = {
    val creatureModelsToCreate =
      newGameState.creatures.keys.toSet -- oldGameState.creatures.keys.toSet

    val creatureModelsToRemove =
      oldGameState.creatures.keys.toSet -- newGameState.creatures.keys.toSet

    val abilityModelsToCreate =
      newGameState.abilities.keys.toSet -- oldGameState.abilities.keys.toSet

    val abilityModelsToRemove =
      oldGameState.abilities.keys.toSet -- newGameState.abilities.keys.toSet

    creatureModelsToCreate.foreach(
      modelEventsScheduler.scheduleCreatureModelAdded
    )
    creatureModelsToRemove.foreach(
      modelEventsScheduler.scheduleCreatureModelRemoved
    )
    abilityModelsToCreate.foreach(
      modelEventsScheduler.scheduleAbilityModelAdded
    )
    abilityModelsToRemove.foreach(
      modelEventsScheduler.scheduleAbilityModelRemoved
    )
  }

  private def processModelEvents(gameState: GameState): Unit = {
    modelEventsScheduler
      .pollCreatureModelAdded()
      .foreach(creatureId => {
        view.createCreatureRenderer(creatureId, gameState)
        physics.createCreatureBody(creatureId, gameState)
      })
    modelEventsScheduler
      .pollCreatureModelRemoved()
      .foreach(creatureId => {
        view.removeCreatureRenderer(creatureId, gameState)
        physics.removeCreatureBody(creatureId, gameState)
      })
    modelEventsScheduler
      .pollAbilityModelAdded()
      .foreach(abilityId => {
        view.createAbilityRenderer(abilityId, gameState)
        physics.createAbilityBody(abilityId, gameState)
      })
    modelEventsScheduler
      .pollAbilityModelRemoved()
      .foreach(abilityId => {
        view.removeAbilityRenderer(abilityId, gameState)
        physics.removeAbilityBody(abilityId, gameState)
      })
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = view.resize(width, height)

  def currentGameState: GameState = gameState

  def overrideGameState(gameState: GameState): Unit = {
    this.gameState = gameState
  }

}
