package com.mygdx.game.view

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.game.Constants
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
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
      Constants.ViewportWorldWidth,
      Constants.ViewportWorldHeight,
      camera
    )
  }

  def setProjectionMatrix(batch: SpriteBatch): Unit = {
    batch.setProjectionMatrix(camera.combined)
  }

  def updateCamera(
      creatureId: Option[EntityId[Creature]],
      gameState: GameState
  ): Unit = {
    val camPosition = camera.position

    val cameraPos = creatureId
      .filter(gameState.creatures.contains)
      .map(gameState.creatures(_).pos)
      .getOrElse(Vector2(0, 0))

    val pos = coordinateTransformation(cameraPos)

    camPosition.x = (math.floor(pos.x * 100) / 100).toFloat
    camPosition.y = (math.floor(pos.y * 100) / 100).toFloat

    camera.update()
  }

  def updateSize(width: Int, height: Int): Unit = {
    viewport.update(width, height)
  }

  def renderB2Debug(debugRenderer: Box2DDebugRenderer, b2World: World): Unit = {
    debugRenderer.render(b2World, camera.combined)
  }

  def getCameraPos: (Float, Float) = {
    (camera.position.x, camera.position.y)
  }

  def createStage(batch: SpriteBatch): Stage =
    new Stage(viewport, batch.spriteBatch)

}
