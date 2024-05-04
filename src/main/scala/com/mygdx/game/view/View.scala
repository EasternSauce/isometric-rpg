package com.mygdx.game.view

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.mygdx.game.Constants
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.inventory.ItemMoveLocation.ItemMoveLocation
import com.mygdx.game.view.inventory._

case class View() {

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _

  private var tiledMap: TiledMap = _

  private var inventoryScene: InventoryStage = _

  private var viewportManager: ViewportManager = _

  def init(
      worldSpriteBatch: SpriteBatch,
      worldTextSpriteBatch: SpriteBatch,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    this.tiledMap = game.gameplay.tiledMap

    creatureRenderers = Map()

    creatureRenderers.values.foreach(_.init(game.gameplay.gameState))

    abilityRenderers = Map()

    viewportManager = ViewportManager()
    viewportManager.init()

    inventoryScene = InventoryStage()

    inventoryScene.init(viewportManager, hudBatch, game)
  }

  def draw(
      worldSpriteBatch: SpriteBatch,
      worldTextSpriteBatch: SpriteBatch,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    viewportManager.draw(worldSpriteBatch, worldTextSpriteBatch, hudBatch)

    worldSpriteBatch.begin()

    renderWorldElementsByPriority(worldSpriteBatch, game)

    abilityRenderers.values.foreach(
      _.render(worldSpriteBatch, game.gameplay.gameState)
    )

    creatureRenderers.values.foreach(
      _.renderLifeBar(worldSpriteBatch, game.gameplay.gameState)
    )

    worldSpriteBatch.end()

    worldTextSpriteBatch.begin()

    creatureRenderers.values.foreach(
      _.renderPlayerName(
        worldTextSpriteBatch,
        game.scene2dSkin.getFont("default-font"),
        game.gameplay.gameState
      )
    )

    worldTextSpriteBatch.end()

    if (Constants.EnableDebug) {
      viewportManager.renderDebug(game.gameplay.physics.getWorld)
    }

    hudBatch.begin()

    //...

    hudBatch.end()

    inventoryScene.draw(game)

  }

  private def renderWorldElementsByPriority(
      worldSpriteBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    val layer0Cells = tiledMap.getLayerCells(0)
    val layer1Cells = tiledMap.getLayerCells(1)

    layer0Cells.foreach(_.render(worldSpriteBatch, game.gameplay.gameState))

    def distanceFromCameraPlane(pos: Vector2): Float = {
      Math.abs(-pos.x + pos.y + tiledMap.getMapWidth) / Math.sqrt(2).toFloat
    }

    val sortFunction: Ordering[Renderable] =
      (renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(game.gameplay.gameState)
        val posB = renderableB.pos(game.gameplay.gameState)

        distanceFromCameraPlane(posB).compare(distanceFromCameraPlane(posA))
      }

    val aliveCreatureRenderables =
      game.gameplay.gameState.creatures
        .filter { case (_, creature) =>
          creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    val deadCreatureRenderables =
      game.gameplay.gameState.creatures
        .filter { case (_, creature) =>
          !creature.alive && creatureRenderers.contains(creature.id)
        }
        .keys
        .toList
        .map(creatureId => creatureRenderers(creatureId))

    deadCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, game.gameplay.gameState))

    (layer1Cells ++ aliveCreatureRenderables)
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, game.gameplay.gameState))
  }

  def update(delta: Float, game: CoreGame): Unit = {
    synchronizeWithGameState(game.gameplay.gameState)

    val creatureId = game.clientCreatureId

    viewportManager.updateCamera(creatureId, game)

    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          inventoryScene.setStageAsInputProcessor()
          inventoryScene.actStage(delta)
        } else {
          Gdx.input.setInputProcessor(new InputAdapter())
        }
      case None =>
    }

    inventoryScene.update(game)
  }

  private def synchronizeWithGameState(gameState: GameState): Unit = {
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

  def resize(width: Int, height: Int): Unit = {
    viewportManager.resize(width, height)
  }

  def unprojectHudCamera(screenCoords: Vector3): Unit =
    viewportManager.unprojectHudCamera(screenCoords)

  def setInventoryHoverItemInfoText(itemInfoText: String): Unit =
    inventoryScene.setHoverItemInfoText(itemInfoText)

  def inventoryCursorPickUpItem(
      pos: Int,
      itemMoveLocation: ItemMoveLocation,
      game: CoreGame
  ): Unit = inventoryScene.cursorPickUpItem(pos, itemMoveLocation, game)

}
