package com.mygdx.game.view

import com.badlogic.gdx.utils.ScreenUtils
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Vector2
import com.mygdx.game.{ClientInformation, Constants}

case class View() {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var levelMap: LevelMap = _

  def init(
      clientInformation: ClientInformation,
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {
    this.levelMap = levelMap

    creatureRenderers = Map(
      clientInformation.clientCreatureId -> CreatureRenderer(
        clientInformation.clientCreatureId
      )
    )

    creatureRenderers.values.foreach(_.init(gameState))

    worldViewport.init(
      1,
      pos => IsometricProjection.translateIsoToScreen(pos)
    )
    b2DebugViewport.init(0.02f, Predef.identity)
  }

  def draw(
      batch: SpriteBatch,
      physics: Physics,
      gameState: GameState
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    worldViewport.setProjectionMatrix(batch)

    batch.begin()

    val layer0Cells = levelMap.getLayerCells(0)
    val layer1Cells = levelMap.getLayerCells(1)

    layer0Cells.foreach(_.render(batch, gameState))

    val creatureRenderables =
      gameState.creatures.keys.toList.map(creatureId =>
        creatureRenderers(creatureId)
      )

    val overgroundRenderables = layer1Cells ++ creatureRenderables

    def distanceFromCameraPlane(pos: Vector2): Float = {
      Math.abs(-pos.x + pos.y + levelMap.getMapWidth) / Math.sqrt(2).toFloat
    }

    overgroundRenderables
      .sorted((renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(gameState)
        val posB = renderableB.pos(gameState)

        distanceFromCameraPlane(posB).compare(distanceFromCameraPlane(posA))
      })
      .foreach(_.render(batch, gameState))

    batch.end()

    if (Constants.EnableDebug) physics.getWorld.renderDebug(b2DebugViewport)

  }

  def update(
      clientInformation: ClientInformation,
      gameState: GameState
  ): Unit = {
    val renderersToCreate =
      gameState.creatures.keys.toSet -- creatureRenderers.keys.toSet

    renderersToCreate.foreach(creatureId => {
      val creatureRenderer = CreatureRenderer(creatureId)
      creatureRenderer.init(gameState)
      creatureRenderers =
        creatureRenderers.updated(creatureId, creatureRenderer)
    })

    worldViewport.updateCamera(clientInformation.clientCreatureId, gameState)
    b2DebugViewport.updateCamera(clientInformation.clientCreatureId, gameState)
  }

  def resize(width: Int, height: Int): Unit = {
    worldViewport.update(width, height)
    b2DebugViewport.update(width, height)
  }

}
