package com.mygdx.game.view

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.util.Vector2

case class Viewport() {
  private var camera: OrthographicCamera = _
  private var viewport: FitViewport = _
  private var coordinateTransformation: Vector2 => Vector2 = _

  import com.badlogic.gdx.graphics.OrthographicCamera

  def init(
      zoom: Float,
      coordinateTransformation: Vector2 => Vector2
  ): Unit = {
    camera = new OrthographicCamera()
    camera.zoom = zoom

    this.coordinateTransformation = coordinateTransformation

    viewport = new FitViewport(
      Constants.ViewpointWorldWidth,
      Constants.ViewpointWorldHeight,
      camera
    )
  }

  def setProjectionMatrix(batch: SpriteBatch): Unit = {
    batch.setProjectionMatrix(camera.combined)
  }

  def updateCamera(
      playerCreatureId: EntityId[Creature],
      gameState: GameState
  ): Unit = {
    val camPosition = camera.position

    val creature = gameState.creatures(playerCreatureId)
    val pos = coordinateTransformation(creature.params.pos)

    camPosition.x = (math.floor(pos.x * 100) / 100).toFloat
    camPosition.y = (math.floor(pos.y * 100) / 100).toFloat

    camera.update()
  }

  def update(width: Int, height: Int): Unit = {
    viewport.update(width, height)
  }

  def renderB2Debug(debugRenderer: Box2DDebugRenderer, b2World: World): Unit = {
    debugRenderer.render(b2World, camera.combined)
  }

  def getCameraPos: (Float, Float) = {
    (camera.position.x, camera.position.y)
  }

}
