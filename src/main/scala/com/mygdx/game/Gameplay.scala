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
  private var gameState: GameState = _

  def init(): Unit = {
    gameState = GameState.initialState(clientInformation)

    levelMap.init()
    view.init(clientInformation, levelMap, gameState)
    physics.init(clientInformation, levelMap, gameState)

    spriteBatch.init()
  }

  def update(delta: Float): Unit = {
    val input = Input.poll()

    physics.update(gameState)
    updateGameState(
      physics.getCreaturePositions,
      physics.getAbilityPositions,
      input,
      delta
    )
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
    gameState = gameState.update(
      creaturePositions,
      abilityPositions,
      input,
      clientInformation,
      physics,
      delta
    )
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = view.resize(width, height)
}
