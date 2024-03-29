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
  private var worldSpriteBatch: SpriteBatch = _
  private var worldTextSpriteBatch: SpriteBatch = _
  private var hudBatch: SpriteBatch = _

  private var _gameState: GameState = _

  private var _scheduledExternalEvents: List[GameStateEvent] = List()

  private var _scheduledPlayersToCreate: List[String] = List()

  def init(): Unit = {
    _gameState = GameState.initialState()

    _levelMap = LevelMap()
    _levelMap.init()

    worldSpriteBatch = SpriteBatch()
    worldSpriteBatch.init()

    worldTextSpriteBatch = SpriteBatch()
    worldTextSpriteBatch.init()

    hudBatch = SpriteBatch()
    hudBatch.init()

    _view = View()
    _view.init(worldSpriteBatch, worldTextSpriteBatch, hudBatch, game)

    _physics = Physics()
    _physics.init(_levelMap, _gameState)

  }

  def update(delta: Float, input: Input): Unit = {
    game.handleInput(input)

    physics.update(gameState)

    updateGameState(
      delta
    )
  }

  def render(delta: Float, input: Input): Unit = {
    view.update(delta, game)

    view.draw(
      worldSpriteBatch,
      worldTextSpriteBatch,
      hudBatch,
      game
    )

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

    worldSpriteBatch.begin()

    gameState.creatures.values
      .map(_.pos)
      .foreach(pos => {
        val worldPos = IsometricProjection.translatePosIsoToScreen(pos)
        worldSpriteBatch.filledRectangle(
          Rectangle(worldPos.x - 5, worldPos.y - 5, 10, 10),
          Color.CYAN
        )
      })

    val mouseScreenPos =
      IsometricProjection.translatePosIsoToScreen(mouseWorldPos)

    worldSpriteBatch.filledRectangle(
      Rectangle(mouseScreenPos.x - 5, mouseScreenPos.y - 5, 10, 10),
      Color.CYAN
    )
    worldSpriteBatch.end()
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
    worldSpriteBatch.dispose()
    worldTextSpriteBatch.dispose()
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

  def schedulePlayerToCreate(clientId: String): Unit = {
    _scheduledPlayersToCreate = _scheduledPlayersToCreate.appended(clientId)
  }

  def scheduledPlayersToCreate: List[String] =
    _scheduledPlayersToCreate

  def clearScheduledPlayersToCreate(): Unit = {
    _scheduledPlayersToCreate = List()
  }

  def gameState: GameState = _gameState
  def levelMap: LevelMap = _levelMap
  def physics: Physics = _physics
  def view: View = _view
}
