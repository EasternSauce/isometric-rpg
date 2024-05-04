package com.mygdx.game.view

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.physics.World

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

  def draw(
      worldSpriteBatch: SpriteBatch,
      worldTextSpriteBatch: SpriteBatch,
      hudBatch: SpriteBatch
  ): Unit = {
    worldViewport.setProjectionMatrix(worldSpriteBatch)

    worldTextViewport.setProjectionMatrix(worldTextSpriteBatch)

    hudViewport.setProjectionMatrix(hudBatch)
  }

  def updateCamera(
      creatureId: Option[EntityId[Creature]],
      game: CoreGame
  ): Unit = {
    worldViewport.updateCamera(creatureId, game.gameplay.gameState)
    b2DebugViewport.updateCamera(creatureId, game.gameplay.gameState)
    worldTextViewport.updateCamera(creatureId, game.gameplay.gameState)
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

}
