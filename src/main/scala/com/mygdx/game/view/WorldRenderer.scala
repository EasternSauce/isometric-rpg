package com.mygdx.game.view

import com.mygdx.game.SpriteBatches
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2

case class WorldRenderer() {

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _

  def init(game: CoreGame): Unit = {
    creatureRenderers = Map()

    creatureRenderers.values.foreach(_.init(game.gameState))

    abilityRenderers = Map()
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

    abilityRenderers.values.foreach(
      _.render(spriteBatches.worldSpriteBatch, worldCameraPos, game.gameState)
    )

    creatureRenderers.values.foreach(
      _.renderLifeBar(spriteBatches.worldSpriteBatch, game.gameState)
    )

    spriteBatches.worldSpriteBatch.end()

    spriteBatches.worldTextSpriteBatch.begin()

    creatureRenderers.values.foreach(
      _.renderPlayerName(
        spriteBatches.worldTextSpriteBatch,
        game.scene2dSkin.getFont("default-font"),
        game.gameState
      )
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
      game.gameState.creatures
        .filter { case (_, creature) =>
          creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    val deadCreatureRenderables =
      game.gameState.creatures
        .filter { case (_, creature) =>
          !creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    deadCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, worldCameraPos, game.gameState))

    aliveCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, worldCameraPos, game.gameState))
  }

  def update(gameState: GameState): Unit = {
    val creatureRenderersToCreate =
      gameState.activeCreatureIds -- creatureRenderers.keys.toSet
    val creatureRenderersToDestroy =
      creatureRenderers.keys.toSet -- gameState.activeCreatureIds

    creatureRenderersToCreate.foreach(createCreatureRenderer(_, gameState))
    creatureRenderersToDestroy.foreach(destroyCreatureRenderer(_, gameState))

    val abilityRenderersToCreate =
      gameState.abilities.keys.toSet -- abilityRenderers.keys.toSet
    val abilityRenderersToDestroy =
      abilityRenderers.keys.toSet -- gameState.abilities.keys.toSet

    abilityRenderersToCreate.foreach(createAbilityRenderer(_, gameState))
    abilityRenderersToDestroy.foreach(destroyAbilityRenderer(_, gameState))
  }

  private def createCreatureRenderer(
      creatureId: EntityId[Creature],
      gameState: GameState
  ): Unit = {
    val creatureRenderer = CreatureRenderer(creatureId)
    creatureRenderer.init(gameState)
    creatureRenderers = creatureRenderers.updated(creatureId, creatureRenderer)
  }

  private def destroyCreatureRenderer(
      creatureId: EntityId[Creature],
      gameState: GameState
  ): Unit = {
    creatureRenderers = creatureRenderers.removed(creatureId)
  }

  private def createAbilityRenderer(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    val abilityRenderer = AbilityRenderer(abilityId)
    abilityRenderer.init(gameState)
    abilityRenderers = abilityRenderers.updated(abilityId, abilityRenderer)
  }

  private def destroyAbilityRenderer(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    abilityRenderers = abilityRenderers.removed(abilityId)
  }

}
