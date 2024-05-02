package com.mygdx.game.view.inventory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.event.gamestate.CreatureCursorPickUpItemEvent
import com.mygdx.game.view.inventory.ItemMoveLocation.{Inventory, ItemMoveLocation}
import com.mygdx.game.view.{SpriteBatch, Viewport}
import com.mygdx.game.{Assets, Constants}

case class InventoryStage() {

  private var stage: Stage = _

  private var window: InventoryWindow = _

  private var itemCursorPickupState: Option[ItemCursorPickupState] = None

  def init(
      hudViewport: Viewport,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    stage = hudViewport.createStage(hudBatch)

    window = InventoryWindow()

    window.init(this, game)
  }

  def draw(game: CoreGame): Unit = {
    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          stage.draw()

          stage.getBatch.begin()
          drawItemOnCursor(game)
          stage.getBatch.end()
        }
      case None =>
    }
  }

  def update(game: CoreGame): Unit = {
    if (game.clientCreature(game.gameplay.gameState).nonEmpty) {
      window.update(itemCursorPickupState, game)
    }
  }

  def setStageAsInputProcessor(): Unit = Gdx.input.setInputProcessor(stage)

  def actStage(delta: Float): Unit = stage.act(delta)

  def setHoverItemInfoText(hoverText: String): Unit = {
    window.setHoverItemInfoText(hoverText)
  }

  def cursorPickUpItem(
      pos: Int,
      itemMoveLocation: ItemMoveLocation,
      game: CoreGame
  ): Unit = {
    if (itemCursorPickupState.isDefined) {
      game.sendEvent(
        CreatureCursorPickUpItemEvent(
          game.clientCreatureId.get,
          itemCursorPickupState.get.itemMoveLocation,
          itemCursorPickupState.get.pos,
          itemMoveLocation,
          pos
        )
      )
      itemCursorPickupState = None
    } else {
      if (
        game.gameplay.gameState
          .creatures(game.clientCreatureId.get)
          .params
          .inventoryItems
          .contains(pos)
      ) {
        itemCursorPickupState = Some(
          ItemCursorPickupState(itemMoveLocation, pos)
        )
      }
    }
  }

  def drawItemOnCursor(game: CoreGame): Unit = {
    if (itemCursorPickupState.nonEmpty) {
      val mousePos = game.mousePos()
      val iconPos =
        if (itemCursorPickupState.get.itemMoveLocation == Inventory) {
          game
            .clientCreature(game.gameplay.gameState)
            .get
            .params
            .inventoryItems(itemCursorPickupState.get.pos)
            .template
            .iconPos
        } else {
          game
            .clientCreature(game.gameplay.gameState)
            .get
            .params
            .equipmentItems(itemCursorPickupState.get.pos)
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
  }

  def addActor(actor: Actor): Unit = stage.addActor(actor)
}
