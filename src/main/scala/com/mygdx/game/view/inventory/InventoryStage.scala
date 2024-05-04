package com.mygdx.game.view.inventory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.event.gamestate.CreaturePutItemOnCursorEvent
import com.mygdx.game.view.inventory.ItemMoveLocation.{Inventory, ItemMoveLocation}
import com.mygdx.game.view.{SpriteBatch, ViewportManager}
import com.mygdx.game.{Assets, Constants}

case class InventoryStage() {

  private var stage: Stage = _

  private var window: InventoryWindow = _

  private var itemPutOnCursorState: Option[ItemCursorPickupState] = None

  def init(
      viewportManager: ViewportManager,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    stage = viewportManager.createHudStage(hudBatch)

    window = InventoryWindow()

    window.init(this, game)
  }

  def draw(game: CoreGame): Unit = {
    stage.draw()
    drawItemOnCursor(game)
  }

  def update(delta: Float, game: CoreGame): Unit = {
    game.clientPlayerState(game.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          stage.act(delta)
          if (game.clientCreature(game.gameState).nonEmpty) {
            window.update(itemPutOnCursorState, game)
          }
        }
      case None =>
    }
  }

  def setStageAsInputProcessor(): Unit = Gdx.input.setInputProcessor(stage)

  def setHoverItemInfoText(hoverText: String): Unit = {
    window.setHoverItemInfoText(hoverText)
  }

  def cursorPickUpItem(
      pos: Int,
      itemMoveLocation: ItemMoveLocation,
      game: CoreGame
  ): Unit = {
    if (itemPutOnCursorState.isDefined) {
      game.sendEvent(
        CreaturePutItemOnCursorEvent(
          game.clientCreatureId.get,
          itemPutOnCursorState.get.itemMoveLocation,
          itemPutOnCursorState.get.pos,
          itemMoveLocation,
          pos
        )
      )
      itemPutOnCursorState = None
    } else {
      if (
        game.gameState
          .creatures(game.clientCreatureId.get)
          .params
          .inventoryItems
          .contains(pos)
      ) {
        itemPutOnCursorState = Some(
          ItemCursorPickupState(itemMoveLocation, pos)
        )
      }
    }
  }

  def drawItemOnCursor(game: CoreGame): Unit = {
    stage.getBatch.begin()

    if (itemPutOnCursorState.nonEmpty) {
      val mousePos = game.mousePos()
      val iconPos =
        if (itemPutOnCursorState.get.itemMoveLocation == Inventory) {
          game
            .clientCreature(game.gameState)
            .get
            .params
            .inventoryItems(itemPutOnCursorState.get.pos)
            .template
            .iconPos
        } else {
          game
            .clientCreature(game.gameState)
            .get
            .params
            .equipmentItems(itemPutOnCursorState.get.pos)
            .template
            .iconPos
        }
      stage.getBatch.draw(
        Assets.getIcon(iconPos.x, iconPos.y),
        mousePos.x - Constants.InventorySlotSize / 2,
        mousePos.y - Constants.InventorySlotSize / 2,
        Constants.InventorySlotSize,
        Constants.InventorySlotSize
      )
    }

    stage.getBatch.end()
  }

  def addActor(actor: Actor): Unit = stage.addActor(actor)
}
