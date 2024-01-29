package com.mygdx.game

import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.input.Input
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.{SpriteBatch, View}

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
    updateGameState(physics.getCreaturePositions, input, delta)
    view.update(clientInformation, gameState)

    view.draw(spriteBatch, physics, gameState)
  }

  private def updateGameState(
      creaturePositions: Map[EntityId[Creature], Vector2],
      input: Input,
      delta: Float
  ): Unit = {
    gameState = gameState.update(
      creaturePositions,
      input,
      clientInformation,
      delta
    )
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = view.resize(width, height)
}
