package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.input.Input
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.view.{IsometricProjection, SpriteBatch, View}

case class Gameplay(game: CoreGame) {

  private var _levelMap: LevelMap = _
  private var _physics: Physics = _
  private var _view: View = _
  private var spriteBatch: SpriteBatch = _

  private var _gameState: GameState = _

  private var _scheduledExternalEvents: List[GameStateEvent] = List()

  private var _scheduledPlayerCreaturesToCreate: List[String] = List()

  def init(): Unit = {
    _gameState = GameState.initialState()

    _levelMap = LevelMap()
    _levelMap.init()

    _view = View()
    _view.init(_levelMap, _gameState)

    _physics = Physics()
    _physics.init(_levelMap, _gameState)

    spriteBatch = SpriteBatch()
    spriteBatch.init()
  }

  def update(input: Input, delta: Float): Unit = {
    game.handleInput(input)

    physics.update(gameState)

    updateGameState(
      delta
    )
  }

  def render(input: Input): Unit = {
    view.update(game)

    view.draw(spriteBatch, physics, gameState)

    if (Constants.EnableDebug) drawMouseAimDebug(input)
  }

  private def drawMouseAimDebug(input: Input): Unit = {
    val mousePos = input.mousePos

    val isoMousePos =
      IsometricProjection.translatePosScreenToIso(mousePos)

    val creature = game.clientCreature(gameState)

    val cameraPos = creature
      .map(_.pos)
      .getOrElse(Vector2(0, 0))

    val mouseWorldPos = cameraPos
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
      delta: Float
  ): Unit = {
    val newGameState = gameState.update(
      delta,
      game
    )

    _gameState = newGameState
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = _view.resize(width, height)

  def overrideGameState(gameState: GameState): Unit = {
    this._gameState = gameState
  }

  def scheduleExternalEvent(gameStateEvents: List[GameStateEvent]): Unit = {
    _scheduledExternalEvents =
      _scheduledExternalEvents.appendedAll(gameStateEvents)
  }

  def externalEvents: List[GameStateEvent] = {
    _scheduledExternalEvents
  }

  def clearExternalEventsQueue(): Unit = {
    _scheduledExternalEvents = List()
  }

  def schedulePlayerCreaturesToCreate(clientId: String): Unit = {
    _scheduledPlayerCreaturesToCreate =
      _scheduledPlayerCreaturesToCreate.appended(clientId)
  }

  def scheduledPlayerCreaturesToCreate: List[String] =
    _scheduledPlayerCreaturesToCreate

  def clearScheduledPlayerCreaturesToCreate(): Unit = {
    _scheduledPlayerCreaturesToCreate = List()
  }

  def gameState: GameState = _gameState
  def levelMap: LevelMap = _levelMap
  def physics: Physics = _physics
  def view: View = _view
}
