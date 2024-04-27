package com.mygdx.game.view

import com.badlogic.gdx.scenes.scene2d.ui.{TextArea, TextButton, TextField, Window}
import com.badlogic.gdx.scenes.scene2d.utils.{ClickListener, TextureRegionDrawable}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.{Align, ScreenUtils}
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.gamestate.PlayerToggleInventoryEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.inventory.{EquipmentItemsActor, EquipmentSlotsActor, InventoryItemsActor, InventorySlotsActor}
import com.mygdx.game.{Assets, Constants}

case class View() {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()
  private val worldTextViewport: Viewport = Viewport()
  private val hudViewport: Viewport = Viewport()

  private var inventoryStage: Stage = _
  private var inventorySlotsActor: InventorySlotsActor = _
  private var equipmentSlotsActor: EquipmentSlotsActor = _
  private var inventoryItemsActor: InventoryItemsActor = _
  private var equipmentItemsActor: EquipmentItemsActor = _

  private var hoverItemInfo: TextField = _

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

    val inventoryWindow = new Window("Inventory", game.scene2dSkin)

    inventoryWindow.setX(800)
    inventoryWindow.setY(200)
    inventoryWindow.setWidth(800)
    inventoryWindow.setHeight(600)

    inventoryWindow.setKeepWithinStage(true)

    hoverItemInfo = new TextArea("", game.scene2dSkin)
    hoverItemInfo.setX(Constants.hoverItemInfoX)
    hoverItemInfo.setY(Constants.hoverItemInfoY)
    hoverItemInfo.setWidth(Constants.hoverItemInfoWidth)
    hoverItemInfo.setHeight(Constants.hoverItemInfoHeight)
    hoverItemInfo.setAlignment(Align.topLeft)
    hoverItemInfo.setTouchable(null)

    inventoryWindow.addActor(hoverItemInfo)

    val exitButton = new TextButton("Exit", game.scene2dSkin)
    exitButton.setX(650)
    exitButton.setY(10)
    exitButton.setWidth(120)
    exitButton.setHeight(30)

    exitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        game.sendEvent(PlayerToggleInventoryEvent(game.clientCreatureId.get))
      }
    })

    inventoryWindow.addActor(exitButton)

    inventorySlotsActor = InventorySlotsActor()
    equipmentSlotsActor = EquipmentSlotsActor()
    inventoryItemsActor = InventoryItemsActor()
    equipmentItemsActor = EquipmentItemsActor()

    inventorySlotsActor.init(game)
    equipmentSlotsActor.init(game)
    inventoryItemsActor.init(game)
    equipmentItemsActor.init(game)

    inventorySlotsActor.addToWindow(inventoryWindow)
    equipmentSlotsActor.addToWindow(inventoryWindow)
    inventoryItemsActor.addToWindow(inventoryWindow)
    equipmentItemsActor.addToWindow(inventoryWindow)

    inventoryStage.addActor(inventoryWindow)
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
        game.scene2dSkin.getFont("default-font"),
        game.gameplay.gameState
      )
    )

    worldTextSpriteBatch.end()

    if (Constants.EnableDebug)
      game.gameplay.physics.getWorld.renderDebug(b2DebugViewport)

    hudBatch.begin()

    // ...

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

    val inventoryItems = game
      .clientCreature(game.gameplay.gameState)
      .get
      .params
      .inventoryItems

    val equipmentItems = game
      .clientCreature(game.gameplay.gameState)
      .get
      .params
      .equipmentItems

    inventoryItemsActor.items.foreach { case (pos, actor) =>
      if (inventoryItems.contains(pos)) {
        val iconPos = inventoryItems(pos).template.iconPos
        actor.setDrawable(
          new TextureRegionDrawable(
            new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
          )
        )
      } else {
        actor.setDrawable(
          new TextureRegionDrawable(Assets.atlas.findRegion("inventory_slot"))
        )
      }
    }

    equipmentItemsActor.items.foreach { case (pos, actor) =>
      if (equipmentItems.contains(pos)) {
        val iconPos = equipmentItems(pos).template.iconPos
        actor.setDrawable(
          new TextureRegionDrawable(
            new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
          )
        )
      } else {
        actor.setDrawable(
          new TextureRegionDrawable(Assets.atlas.findRegion("inventory_slot"))
        )
      }
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

  def setHoverItemInfoText(hoverText: String): Unit = {
    hoverItemInfo.setText(hoverText)
  }
}
