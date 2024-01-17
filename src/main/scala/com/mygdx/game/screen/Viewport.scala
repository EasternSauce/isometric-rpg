package com.mygdx.game.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState

case class Viewport() {
  private var camera: OrthographicCamera = _
  private var viewport: FitViewport = _
  private var coordinateTransformation: (Float, Float) => (Float, Float) = _

  import com.badlogic.gdx.graphics.OrthographicCamera

  def init(
      zoom: Float,
      coordinateTransformation: (Float, Float) => (Float, Float)
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

  def updateCamera(playerCreatureId: String, gameState: GameState): Unit = {
    val camPosition = camera.position

    val creature = gameState.creatures(playerCreatureId)
    val (x, y) =
      coordinateTransformation(creature.params.x, creature.params.y)

    camPosition.x = (math.floor(x * 100) / 100).toFloat
    camPosition.y = (math.floor(y * 100) / 100).toFloat

    camera.update()
  }

  def update(width: Int, height: Int): Unit = {
    viewport.update(width, height)
  }

  def renderB2Debug(debugRenderer: Box2DDebugRenderer, b2World: World): Unit = {
    debugRenderer.render(b2World, camera.combined)
  }

}
