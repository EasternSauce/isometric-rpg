package com.mygdx.game

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.view.{SpriteBatch, View}

case class Gameplay() {
  private val clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = "creature1")

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
    val (playerPosX, playerPosY) = physics.getPlayerPos
    gameState = gameState.update(
      clientInformation,
      playerPosX,
      playerPosY,
      delta
    )

    view.update(clientInformation, gameState)
    physics.update(gameState)

    view.draw(spriteBatch, physics, gameState)
  }

  def dispose(): Unit = {
    spriteBatch.dispose()
  }

  def resize(width: Int, height: Int): Unit = view.resize(width, height)
}