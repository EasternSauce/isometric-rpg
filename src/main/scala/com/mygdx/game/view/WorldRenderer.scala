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
      game
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

  private def renderWorldElementsByPriority(
      worldSpriteBatch: SpriteBatch,
      worldCameraPos: Vector2,
      game: CoreGame
  ): Unit = {
    val tiledMap: TiledMap = game.gameplay.tiledMap

    val layer0Cells = tiledMap.getLayer("fill")
    val layer1Cells = tiledMap.getLayer("background")
    val layer2Cells = tiledMap.getLayer("object")
    val layer3Cells = tiledMap.getLayer("manual_object")

    layer0Cells.foreach(
      _.render(worldSpriteBatch, worldCameraPos, game.gameState)
    )
    layer1Cells.foreach(
      _.render(worldSpriteBatch, worldCameraPos, game.gameState)
    )
    layer2Cells.foreach(
      _.render(worldSpriteBatch, worldCameraPos, game.gameState)
    )
    layer3Cells.foreach(
      _.render(worldSpriteBatch, worldCameraPos, game.gameState)
    )

    def distanceFromCameraPlane(pos: Vector2): Float = {
      Math.abs(-pos.x + pos.y + tiledMap.getMapWidth) / Math.sqrt(2).toFloat
    }

    val sortFunction: Ordering[Renderable] =
      (renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(game.gameState)
        val posB = renderableB.pos(game.gameState)

        distanceFromCameraPlane(posB).compare(distanceFromCameraPlane(posA))
      }

    val aliveCreatureRenderables =
      creatureRenderers.getRenderersForAliveCreatures(game.gameState)

    val deadCreatureRenderables =
      creatureRenderers.getRenderersForDeadCreatures(game.gameState)

    deadCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, worldCameraPos, game.gameState))

    aliveCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, worldCameraPos, game.gameState))
  }

  def update(gameState: GameState): Unit = {
    creatureRenderers.update(gameState)
    abilityRenderers.update(gameState)
  }
}
