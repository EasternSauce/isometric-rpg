package com.mygdx.game

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.input.Input
import com.mygdx.game.physics.Physics
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.view.{IsometricProjection, View}

case class Gameplay(private val game: CoreGame) {

  private var _tiledMaps: Map[AreaId, TiledMap] = _

  private var _physics: Physics = _
  private var _view: View = _

  private var spriteBatches: SpriteBatches = _

  private var _gameState: GameState = _

  private var _scheduledExternalEvents: List[GameStateEvent] = List()

  private var _scheduledPlayersToCreate: List[String] = List()

  def init(): Unit = {
    _tiledMaps =
      Constants.areaIds.map(areaId => (areaId, TiledMap(areaId))).toMap
    _tiledMaps.values.foreach(_.init())

    _gameState = GameState.initialState()

    spriteBatches = SpriteBatches()

    spriteBatches.worldSpriteBatch.init()
    spriteBatches.worldTextSpriteBatch.init()
    spriteBatches.hudBatch.init()

    _view = View()
    _view.init(spriteBatches, game)

    _physics = Physics()
    _physics.init(_tiledMaps, _gameState)
  }

  def update(delta: Float, input: Input): Unit = {
    game.handleInput(input)

    game.updatePhysics()

    updateGameState(delta)
  }

  def render(delta: Float, input: Input): Unit = {
    view.update(delta, game)

    view.draw(spriteBatches, game)

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

    spriteBatches.worldSpriteBatch.begin()

    gameState.creatures.values
      .map(_.pos)
      .foreach(pos => {
        val worldPos = IsometricProjection.translatePosIsoToScreen(pos)
        spriteBatches.worldSpriteBatch.filledRectangle(
          Rectangle(worldPos.x - 5, worldPos.y - 5, 10, 10),
          Color.CYAN
        )
      })

    val mouseScreenPos =
      IsometricProjection.translatePosIsoToScreen(mouseWorldPos)

    spriteBatches.worldSpriteBatch.filledRectangle(
      Rectangle(mouseScreenPos.x - 5, mouseScreenPos.y - 5, 10, 10),
      Color.CYAN
    )
    spriteBatches.worldSpriteBatch.end()
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
    spriteBatches.hudBatch.dispose()
    spriteBatches.worldSpriteBatch.dispose()
    spriteBatches.worldTextSpriteBatch.dispose()
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

  def schedulePlayerToCreate(playerToCreate: String): Unit = {
    _scheduledPlayersToCreate =
      _scheduledPlayersToCreate.appended(playerToCreate)
  }

  def scheduledPlayersToCreate: List[String] =
    _scheduledPlayersToCreate

  def clearScheduledPlayersToCreate(): Unit = {
    _scheduledPlayersToCreate = List()
  }

  def tiledMaps: Map[AreaId, TiledMap] = _tiledMaps
  def gameState: GameState = _gameState
  def physics: Physics = _physics
  def view: View = _view
}
