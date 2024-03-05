package com.mygdx.game.view

import com.badlogic.gdx.utils.ScreenUtils
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Vector2
import com.mygdx.game.{ClientInformation, Constants}

case class View() {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _
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

    abilityRenderers = Map()

    worldViewport.init(
      1,
      pos => IsometricProjection.translatePosIsoToScreen(pos)
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

    val aliveCreatureRenderables =
      gameState.creatures
        .filter { case (_, creature) =>
          creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    val deadCreatureRenderables =
      gameState.creatures
        .filter { case (_, creature) =>
          !creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    def distanceFromCameraPlane(pos: Vector2): Float = {
      Math.abs(-pos.x + pos.y + levelMap.getMapWidth) / Math.sqrt(2).toFloat
    }

    val sortFunction: Ordering[Renderable] =
      (renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(gameState)
        val posB = renderableB.pos(gameState)

        distanceFromCameraPlane(posB).compare(distanceFromCameraPlane(posA))
      }

    deadCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(batch, gameState))

    (layer1Cells ++ aliveCreatureRenderables)
      .sorted(sortFunction)
      .foreach(_.render(batch, gameState))

    abilityRenderers.values.foreach(_.render(batch, gameState))

    creatureRenderers.values.foreach(_.renderLifeBar(batch, gameState))

    batch.end()

    if (Constants.EnableDebug) physics.getWorld.renderDebug(b2DebugViewport)

  }

  def update(
      clientInformation: ClientInformation,
      gameState: GameState
  ): Unit = {
    synchronizeWithGameState(gameState)

    worldViewport.updateCamera(clientInformation.clientCreatureId, gameState)
    b2DebugViewport.updateCamera(clientInformation.clientCreatureId, gameState)
  }

  private def synchronizeWithGameState(gameState: GameState): Unit = {
    val creatureRenderersToCreate =
      gameState.creatures.keys.toSet -- creatureRenderers.keys.toSet
    val creatureRenderersToDestroy =
      creatureRenderers.keys.toSet -- gameState.creatures.keys.toSet

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

  def resize(width: Int, height: Int): Unit = {
    worldViewport.update(width, height)
    b2DebugViewport.update(width, height)
  }
}
