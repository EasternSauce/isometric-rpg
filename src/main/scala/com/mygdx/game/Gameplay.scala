package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.action.GameStateAction
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.input.Input
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.view.{IsometricProjection, SpriteBatch, View}

case class Gameplay(game: CoreGame) {

  private val _clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = EntityId("player"))

  private val _levelMap: LevelMap = LevelMap()
  private val _physics: Physics = Physics()
  private val _view: View = View()
  private val spriteBatch: SpriteBatch = SpriteBatch()
  private val modelEventsScheduler: ModelEventsScheduler =
    ModelEventsScheduler()
  private var _gameState: GameState = _

  def init(): Unit = {
    _gameState = GameState.initialState(_clientInformation)

    _levelMap.init()
    _view.init(_clientInformation, _levelMap, _gameState)
    _physics.init(_clientInformation, _levelMap, _gameState)

    spriteBatch.init()
  }

  def update(input: Input, delta: Float): Unit = {
    _physics.update(_gameState)

    updateGameState(
      _physics.getCreaturePositions,
      _physics.getAbilityPositions,
      input,
      delta
    )

    processModelEvents(_gameState)
  }

  def render(input: Input): Unit = {
    _view.update(_clientInformation, _gameState)

    _view.draw(spriteBatch, _physics, _gameState)

    if (Constants.EnableDebug) drawMouseAimDebug(input)
  }

  private def drawMouseAimDebug(input: Input): Unit = {
    val mousePos = input.mousePos

    val isoMousePos =
      IsometricProjection.translatePosScreenToIso(mousePos)

    val mouseWorldPos = _gameState
      .creatures(_clientInformation.clientCreatureId)
      .params
      .pos
      .add(isoMousePos)

    spriteBatch.begin()

    _gameState.creatures.values
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
    val newGameState = _gameState.update(
      creaturePositions,
      abilityPositions,
      input,
      delta,
      game
    )

    scheduleModelEvents(_gameState, newGameState)

    _gameState = newGameState
  }

  private def scheduleModelEvents(
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
        _view.createCreatureRenderer(creatureId, gameState)
        _physics.createCreatureBody(creatureId, gameState)
      })
    modelEventsScheduler
      .pollCreatureModelRemoved()
      .foreach(creatureId => {
        _view.removeCreatureRenderer(creatureId, gameState)
        _physics.removeCreatureBody(creatureId, gameState)
      })
    modelEventsScheduler
      .pollAbilityModelAdded()
      .foreach(abilityId => {
        _view.createAbilityRenderer(abilityId, gameState)
        _physics.createAbilityBody(abilityId, gameState)
      })
    modelEventsScheduler
      .pollAbilityModelRemoved()
      .foreach(abilityId => {
        _view.removeAbilityRenderer(abilityId, gameState)
        _physics.removeAbilityBody(abilityId, gameState)
      })
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = _view.resize(width, height)

  def overrideGameState(gameState: GameState): Unit = {
    this._gameState = gameState
  }

  def applyActions(actions: List[GameStateAction]): Unit = {
    if (_gameState != null) {
      this._gameState = actions.foldLeft(_gameState) {
        case (gameState, action) =>
          action.applyToGameState(gameState)
      }
    }
  }

  def gameState: GameState = _gameState
  def clientInformation: ClientInformation = _clientInformation
  def levelMap: LevelMap = _levelMap
  def physics: Physics = _physics
  def view: View = _view
}
