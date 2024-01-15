package com.mygdx.game.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.view.tile.Tile

case class Viewport() {

  private var worldCamera: OrthographicCamera = _
  private var worldViewport: FitViewport = _

  def init(): Unit = {
    worldCamera = new OrthographicCamera()

    worldViewport = new FitViewport(
      Constants.ViewpointWorldWidth,
      Constants.ViewpointWorldHeight,
      worldCamera
    )
  }

  def setProjectionMatrix(batch: SpriteBatch): Unit = {
    batch.setProjectionMatrix(worldCamera.combined)
  }

  def updateCamera(playerCreatureId: String, gameState: GameState): Unit = {
    val camPosition = worldCamera.position

    val creature = gameState.creatures(playerCreatureId)
    val (x, y) =
      Tile.convertToIsometricCoordinates(creature.params.x, creature.params.y)

    camPosition.x = (math.floor(x * 100) / 100).toFloat
    camPosition.y = (math.floor(y * 100) / 100).toFloat

    worldCamera.update()
  }

  def update(width: Int, height: Int): Unit = {
    worldViewport.update(width, height)
  }

}
