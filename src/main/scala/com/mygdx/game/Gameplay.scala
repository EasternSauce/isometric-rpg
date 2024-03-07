package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.input.Input
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Rectangle
import com.mygdx.game.view.{IsometricProjection, SpriteBatch, View}

case class Gameplay(game: CoreGame) {

  private val _clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = EntityId("player"))

  private var _levelMap: LevelMap = _
  private var _physics: Physics = _
  private var _view: View = _
  private var spriteBatch: SpriteBatch = _

  private var _gameState: GameState = _

  def init(): Unit = {
    _gameState = GameState.initialState(_clientInformation)

    _levelMap = LevelMap()
    _levelMap.init()

    _view = View()
    _view.init(_clientInformation, _levelMap, _gameState)

    _physics = Physics()
    _physics.init(_clientInformation, _levelMap, _gameState)

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

  def applyBroadcastEvents(broadcastEvents: List[BroadcastEvent]): Unit = {
    if (_gameState != null) {
      this._gameState = broadcastEvents.foldLeft(_gameState) {
        case (gameState, broadcastEvent) =>
          broadcastEvent.applyToGameState(gameState)
      }
    }
  }

  def gameState: GameState = _gameState
  def clientInformation: ClientInformation = _clientInformation
  def levelMap: LevelMap = _levelMap
  def physics: Physics = _physics
  def view: View = _view
}
