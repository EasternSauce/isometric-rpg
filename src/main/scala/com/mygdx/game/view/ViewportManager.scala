package com.mygdx.game.view

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.mygdx.game.SpriteBatches
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.physics.World
import com.mygdx.game.util.Vector2

case class ViewportManager() {

  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()
  private val worldTextViewport: Viewport = Viewport()
  private val hudViewport: Viewport = Viewport()

  def init(): Unit = {
    worldViewport.init(
      1,
      pos => IsometricProjection.translatePosIsoToScreen(pos)
    )
    b2DebugViewport.init(0.02f, Predef.identity)

    worldTextViewport.init(
      1,
      pos => IsometricProjection.translatePosIsoToScreen(pos)
    )

    hudViewport.init(
      1,
      Predef.identity
    )
  }

  def setProjectionMatrices(spriteBatches: SpriteBatches): Unit = {
    worldViewport.setProjectionMatrix(spriteBatches.worldSpriteBatch)
    worldTextViewport.setProjectionMatrix(spriteBatches.worldTextSpriteBatch)
    hudViewport.setProjectionMatrix(spriteBatches.hudBatch)
  }

  def updateCameras(
      creatureId: Option[EntityId[Creature]],
      game: CoreGame
  ): Unit = {
    worldViewport.updateCamera(creatureId, game.gameState)
    b2DebugViewport.updateCamera(creatureId, game.gameState)
    worldTextViewport.updateCamera(creatureId, game.gameState)
  }

  def resize(width: Int, height: Int): Unit = {
    worldViewport.updateSize(width, height)
    b2DebugViewport.updateSize(width, height)
    worldTextViewport.updateSize(width, height)
    hudViewport.updateSize(width, height)
  }

  def createHudStage(hudBatch: SpriteBatch): Stage =
    hudViewport.createStage(hudBatch)

  def renderDebug(world: World): Unit = world.renderDebug(b2DebugViewport)

  def unprojectHudCamera(screenCoords: Vector3): Unit =
    hudViewport.unprojectCamera(screenCoords)

  def getWorldCameraPos: Vector2 = worldViewport.getCameraPos

}
