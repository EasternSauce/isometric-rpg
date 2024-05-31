package com.mygdx.game.view

import com.mygdx.game.SpriteBatches
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2

case class WorldRenderer() {
  private var creatureRenderers: CreatureRenderers = _
  private var abilityRenderers: AbilityRenderers = _

  def init(game: CoreGame): Unit = {
    creatureRenderers = CreatureRenderers()
    creatureRenderers.init(game.gameState)

    abilityRenderers = AbilityRenderers()
    abilityRenderers.init(game.gameState)
  }

  def drawWorld(
      spriteBatches: SpriteBatches,
      worldCameraPos: Vector2,
      game: CoreGame
  ): Unit = {
    spriteBatches.worldSpriteBatch.begin()

    renderWorldElementsByPriority(
      spriteBatches.worldSpriteBatch,
      worldCameraPos,
      game.gameplay.tiledMap,
      game.gameState
    )

    abilityRenderers.renderAbilities(
      spriteBatches,
      worldCameraPos,
      game.gameState
    )

    creatureRenderers.renderLifeBars(spriteBatches, game.gameState)

    spriteBatches.worldSpriteBatch.end()

    spriteBatches.worldTextSpriteBatch.begin()

    creatureRenderers.renderPlayerNames(
      spriteBatches,
      game.scene2dSkin,
      game.gameState
    )

    spriteBatches.worldTextSpriteBatch.end()
  }

  private def distanceFromCameraPlane(mapWidth: Int, pos: Vector2): Float = {
    Math.abs(-pos.x + pos.y + mapWidth) / Math.sqrt(2).toFloat
  }

  private def renderWorldElementsByPriority(
      worldSpriteBatch: SpriteBatch,
      worldCameraPos: Vector2,
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    renderLowPriorityMapTiles(
      worldSpriteBatch,
      worldCameraPos,
      tiledMap,
      gameState
    )
    renderDeadCreatures(worldSpriteBatch, worldCameraPos, tiledMap, gameState)
    renderDynamicElements(worldSpriteBatch, worldCameraPos, tiledMap, gameState)
  }

  private def renderLowPriorityMapTiles(
      worldSpriteBatch: SpriteBatch,
      worldCameraPos: Vector2,
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    val fillCells = tiledMap.getLayer("fill")
    val backgroundCells = tiledMap.getLayer("background")
    val manualObjectBottomCells = tiledMap.getLayer("manual_object_bottom")

    (fillCells ++ backgroundCells ++ manualObjectBottomCells).foreach(
      _.render(worldSpriteBatch, worldCameraPos, gameState)
    )
  }

  private def renderDynamicElements(
      worldSpriteBatch: SpriteBatch,
      worldCameraPos: Vector2,
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    val aliveCreatureRenderables =
      creatureRenderers.getRenderersForAliveCreatures(gameState)

    val objectCells = tiledMap.getLayer("object")
    val manualObjectTopCells = tiledMap.getLayer("manual_object_top")

    (aliveCreatureRenderables ++ objectCells ++ manualObjectTopCells)
      .sorted(worldElementSortFunction(tiledMap.getMapWidth, gameState))
      .foreach(_.render(worldSpriteBatch, worldCameraPos, gameState))
  }

  private def renderDeadCreatures(
      worldSpriteBatch: SpriteBatch,
      worldCameraPos: Vector2,
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    val deadCreatureRenderables =
      creatureRenderers.getRenderersForDeadCreatures(gameState)

    deadCreatureRenderables
      .sorted(worldElementSortFunction(tiledMap.getMapWidth, gameState))
      .foreach(_.render(worldSpriteBatch, worldCameraPos, gameState))
  }

  private def worldElementSortFunction(
      mapWidth: Int,
      gameState: GameState
  ): Ordering[Renderable] = {
    val sortFunction: Ordering[Renderable] =
      (renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(gameState)
        val posB = renderableB.pos(gameState)

        distanceFromCameraPlane(mapWidth, posB).compare(
          distanceFromCameraPlane(mapWidth, posA)
        )
      }
    sortFunction
  }

  def update(gameState: GameState): Unit = {
    creatureRenderers.update(gameState)
    abilityRenderers.update(gameState)
  }
}
