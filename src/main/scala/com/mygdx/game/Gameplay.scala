package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
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

  private var _scheduledBroadcastEvents: List[BroadcastEvent] = List()

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
    _physics.update(_gameState)

    updateGameState(
      input,
      delta
    )
  }

  def render(input: Input): Unit = {
    _view.update(game)

    _view.draw(spriteBatch, _physics, _gameState)

    if (Constants.EnableDebug) drawMouseAimDebug(input)
  }

  private def drawMouseAimDebug(input: Input): Unit = {
    val mousePos = input.mousePos

    val isoMousePos =
      IsometricProjection.translatePosScreenToIso(mousePos)

    val creatureId = game.clientId.map(EntityId[Creature])

    val cameraPos = creatureId
      .filter(gameState.creatures.contains)
      .map(gameState.creatures(_).pos)
      .getOrElse(Vector2(0, 0))

    val mouseWorldPos = cameraPos
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
      input: Input,
      delta: Float
  ): Unit = {
    val newGameState = _gameState.update(
      input,
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

  def scheduleBroadcastEvents(gameStateEvents: List[BroadcastEvent]): Unit = {
    _scheduledBroadcastEvents =
      _scheduledBroadcastEvents.appendedAll(gameStateEvents)
  }

  def scheduledBroadcastEvents: List[BroadcastEvent] = {
    _scheduledBroadcastEvents
  }

  def clearScheduledBroadcastEvents(): Unit = {
    _scheduledBroadcastEvents = List()
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
