package com.mygdx.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.mygdx.game.Constants
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.{Rectangle, Vector2}

case class View() {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()
  private val worldTextViewport: Viewport = Viewport()
  private val hudViewport: Viewport = Viewport()

  private var inventoryStage: Stage = _

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _
  private var levelMap: LevelMap = _

  def init(
      worldSpriteBatch: SpriteBatch,
      worldTextSpriteBatch: SpriteBatch,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    this.levelMap = game.gameplay.levelMap

    creatureRenderers = Map()

    creatureRenderers.values.foreach(_.init(game.gameplay.gameState))

    abilityRenderers = Map()

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

    inventoryStage = hudViewport.createStage(hudBatch)

    inventoryStage.addActor(InventoryBuilder().build())
  }

  def draw(
      worldSpriteBatch: SpriteBatch,
      worldTextSpriteBatch: SpriteBatch,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    worldViewport.setProjectionMatrix(worldSpriteBatch)

    worldTextViewport.setProjectionMatrix(worldTextSpriteBatch)

    hudViewport.setProjectionMatrix(hudBatch)

    worldSpriteBatch.begin()

    val layer0Cells = levelMap.getLayerCells(0)
    val layer1Cells = levelMap.getLayerCells(1)

    layer0Cells.foreach(_.render(worldSpriteBatch, game.gameplay.gameState))

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

    def distanceFromCameraPlane(pos: Vector2): Float = {
      Math.abs(-pos.x + pos.y + levelMap.getMapWidth) / Math.sqrt(2).toFloat
    }

    val sortFunction: Ordering[Renderable] =
      (renderableA: Renderable, renderableB: Renderable) => {
        val posA = renderableA.pos(game.gameplay.gameState)
        val posB = renderableB.pos(game.gameplay.gameState)

        distanceFromCameraPlane(posB).compare(distanceFromCameraPlane(posA))
      }

    deadCreatureRenderables
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, game.gameplay.gameState))

    (layer1Cells ++ aliveCreatureRenderables)
      .sorted(sortFunction)
      .foreach(_.render(worldSpriteBatch, game.gameplay.gameState))

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
        game.skin.getFont("default-font"),
        game.gameplay.gameState
      )
    )

    worldTextSpriteBatch.end()

    if (Constants.EnableDebug)
      game.gameplay.physics.getWorld.renderDebug(b2DebugViewport)

    hudBatch.begin()

    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          hudBatch.filledRectangle(
            Rectangle(
              Constants.InventoryX,
              Constants.InventoryY,
              2 * Constants.InventoryMargin + (Constants.InventorySlotSize + 5) * Constants.InventoryWidth - 5,
              2 * Constants.InventoryMargin + (Constants.InventorySlotSize + 5) * Constants.InventoryHeight - 5
            ),
            new Color(0.2745f, 0.2314f, 0.2235f, 1f)
          )
          hudBatch.filledRectangle(
            Rectangle(
              Constants.EquipmentX,
              Constants.EquipmentY,
              2 * Constants.InventoryMargin + (Constants.InventorySlotSize + 5) - 5,
              2 * Constants.InventoryMargin + (Constants.InventorySlotSize + 5) * Constants.EquipmentSlotCount - 5
            ),
            new Color(0.2745f, 0.2314f, 0.2235f, 1f)
          )
        }
      case None =>
    }

    hudBatch.end()

    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          inventoryStage.draw()
        }
      case None =>
    }

  }

  def update(delta: Float, game: CoreGame): Unit = {
    synchronizeWithGameState(game.gameplay.gameState)

    val creatureId = game.clientCreatureId

    worldViewport.updateCamera(creatureId, game.gameplay.gameState)
    b2DebugViewport.updateCamera(creatureId, game.gameplay.gameState)
    worldTextViewport.updateCamera(creatureId, game.gameplay.gameState)

    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          Gdx.input.setInputProcessor(inventoryStage)
          inventoryStage.act(delta)
        } else {
          Gdx.input.setInputProcessor(new InputAdapter())
        }
      case None =>
    }
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
    worldViewport.updateSize(width, height)
    b2DebugViewport.updateSize(width, height)
    worldTextViewport.updateSize(width, height)
    hudViewport.updateSize(width, height)
  }
}
