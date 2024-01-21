package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.gamestate.GameState

object GameplayScreen extends Screen {

  private val clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = "creature1")

  private val view: View = View(clientInformation)
  private val spriteBatch: SpriteBatch = SpriteBatch()
  private var gameState: GameState = _

  override def show(): Unit = {
    gameState = GameState.initialState(clientInformation)

    view.init(clientInformation, gameState)
    spriteBatch.init()
  }

  override def render(delta: Float): Unit = {
    val (playerPosX, playerPosY) = view.getPlayerPos
    gameState = gameState.update(
      clientInformation,
      playerPosX,
      playerPosY,
      delta
    )

    view.update(clientInformation, gameState)

    view.draw(spriteBatch, gameState)
  }

  override def dispose(): Unit = {
    spriteBatch.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    view.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
