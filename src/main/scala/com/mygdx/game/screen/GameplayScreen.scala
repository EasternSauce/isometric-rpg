package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.gamestate.GameState

object GameplayScreen extends Screen {

  private val clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = "creature1")
  private val viewport: Viewport = Viewport()
  private val view: View = View()
  private val spriteBatch: SpriteBatch = SpriteBatch()
  private var gameState: GameState = _

  override def show(): Unit = {
    gameState = GameState.initialState(clientInformation)

    viewport.init()
    view.init(clientInformation, gameState)
    spriteBatch.init()
  }

  override def render(delta: Float): Unit = {
    gameState =
      gameState.update(clientInformation, KeyboardInput.getInput(), delta)

    view.update(clientInformation, viewport, gameState)

    view.draw(viewport, spriteBatch, gameState)
  }

  override def dispose(): Unit = {
    spriteBatch.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    viewport.update(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
