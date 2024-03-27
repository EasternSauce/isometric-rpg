package com.mygdx.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.{Image, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.{Assets, Constants}

case class View() {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()
  private val worldTextViewport: Viewport = Viewport()
  private val hudViewport: Viewport = Viewport()

  private var inventoryStage: Stage = _

  private var creatureRenderers: Map[EntityId[Creature], CreatureRenderer] = _
  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _
  private var levelMap: LevelMap = _

  private val inventoryX = Gdx.graphics.getWidth / 2 - 150
  private val inventoryY = Gdx.graphics.getHeight / 2 - 150
  private val margin = 15
  private val slotSize = 45
  private val inventoryWidth = 10
  private val inventoryHeight = 5

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

    val exitButton: TextButton = new TextButton("Exit", game.skin, "default")
    exitButton.setX(Gdx.graphics.getWidth / 2 - 100)
    exitButton.setY(160)
    exitButton.setWidth(200)
    exitButton.setHeight(50)
    inventoryStage.addActor(exitButton)

    case class InventorySlotImage(
        atlasRegion: TextureAtlas.AtlasRegion,
        slotX: Int,
        slotY: Int
    ) extends Image(atlasRegion)

    for {
      x <- 0 until inventoryWidth
      y <- 0 until inventoryHeight
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), x, y)

      image.setX(margin + inventoryX + (slotSize + 5) * x)
      image.setY(margin + inventoryY + (slotSize + 5) * y)
      image.setWidth(slotSize)
      image.setHeight(slotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked slot " + image.slotX + " " + image.slotY)
        }
      })

      inventoryStage.addActor(image)
    }

    exitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        System.exit(0)
      }
    })
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
              inventoryX,
              inventoryY,
              2 * margin + (slotSize + 5) * inventoryWidth - 5,
              2 * margin + (slotSize + 5) * inventoryHeight - 5
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
