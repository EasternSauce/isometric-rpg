package com.mygdx.game.view

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.view.inventorywindow.ItemMoveLocation.ItemMoveLocation
import com.mygdx.game.view.inventorywindow._
import com.mygdx.game.{Constants, SpriteBatches}

case class View() {

  private var inventoryWindowStage: InventoryWindowStage = _
  private var viewportManager: ViewportManager = _
  private var worldRenderer: WorldRenderer = _

  def init(
      spriteBatches: SpriteBatches,
      game: CoreGame
  ): Unit = {
    worldRenderer = WorldRenderer()
    worldRenderer.init(game)

    viewportManager = ViewportManager()
    viewportManager.init()

    inventoryWindowStage = InventoryWindowStage()
    inventoryWindowStage.init(viewportManager, spriteBatches.hudBatch, game)
  }

  def draw(
      spriteBatches: SpriteBatches,
      game: CoreGame
  ): Unit = {
    ScreenUtils.clear(0f, 0f, 0f, 1)

    viewportManager.setProjectionMatrices(spriteBatches)

    worldRenderer.drawWorld(
      spriteBatches,
      viewportManager.getWorldCameraPos,
      game
    )

    if (Constants.EnableDebug) {
      viewportManager.renderDebug(game.gameplay.physics.world)
    }

    drawHud(spriteBatches.hudBatch)

    game.clientPlayerState(game.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          inventoryWindowStage.draw(game)
        }
      case None =>
    }

  }

  private def drawHud(hudBatch: SpriteBatch): Unit = {
    hudBatch.begin()

    //...

    hudBatch.end()
  }

  def update(delta: Float, game: CoreGame): Unit = {
    worldRenderer.update(game.gameState)

    val creatureId = game.clientCreatureId

    viewportManager.updateCameras(creatureId, game)

    updateInputProcessor(game)

    inventoryWindowStage.update(delta, game)
  }

  private def updateInputProcessor(game: CoreGame): Unit = {
    game.clientPlayerState(game.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          inventoryWindowStage.setStageAsInputProcessor()
        } else {
          Gdx.input.setInputProcessor(new InputAdapter())
        }
      case None =>
    }
  }

  def resize(width: Int, height: Int): Unit = {
    viewportManager.resize(width, height)
  }

  def unprojectHudCamera(screenCoords: Vector3): Unit =
    viewportManager.unprojectHudCamera(screenCoords)

  def setInventoryHoverItemInfoText(itemInfoText: String): Unit =
    inventoryWindowStage.setHoverItemInfoText(itemInfoText)

  def onSlotClick(
      pos: Int,
      itemMoveLocation: ItemMoveLocation,
      game: CoreGame
  ): Unit = inventoryWindowStage.onSlotClick(pos, itemMoveLocation, game)

}
