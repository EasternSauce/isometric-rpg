package com.mygdx.game.view.inventory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.{TextArea, TextButton, TextField, Window}
import com.badlogic.gdx.scenes.scene2d.utils.{ClickListener, TextureRegionDrawable}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.event.gamestate.{CreatureCursorPickUpItemEvent, PlayerToggleInventoryEvent}
import com.mygdx.game.view.inventory.ItemMoveLocation.{Equipment, Inventory, ItemMoveLocation}
import com.mygdx.game.view.{SpriteBatch, Viewport}
import com.mygdx.game.{Assets, Constants}

case class InventoryStage() {

  private var stage: Stage = _

  private var inventorySlotsActorGroup: InventorySlotsActorGroup = _
  private var equipmentSlotsActorGroup: EquipmentSlotsActorGroup = _
  private var inventoryItemsActorGroup: InventoryItemsActorGroup = _
  private var equipmentItemsActorGroup: EquipmentItemsActorGroup = _

  private var hoverItemInfo: TextField = _

  private var cursorPickUpItem: Option[CursorPickUpItem] = None

  def init(
      hudViewport: Viewport,
      hudBatch: SpriteBatch,
      game: CoreGame
  ): Unit = {
    stage = hudViewport.createStage(hudBatch)

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

    inventorySlotsActorGroup = InventorySlotsActorGroup()
    equipmentSlotsActorGroup = EquipmentSlotsActorGroup()
    inventoryItemsActorGroup = InventoryItemsActorGroup()
    equipmentItemsActorGroup = EquipmentItemsActorGroup()

    inventorySlotsActorGroup.init(game)
    equipmentSlotsActorGroup.init(game)
    inventoryItemsActorGroup.init(game)
    equipmentItemsActorGroup.init(game)

    inventorySlotsActorGroup.addToWindow(inventoryWindow)
    equipmentSlotsActorGroup.addToWindow(inventoryWindow)
    inventoryItemsActorGroup.addToWindow(inventoryWindow)
    equipmentItemsActorGroup.addToWindow(inventoryWindow)

    stage.addActor(inventoryWindow)
  }

  def draw(game: CoreGame): Unit = {
    game.clientPlayerState(game.gameplay.gameState) match {
      case Some(playerState) =>
        if (playerState.inventoryOpen) {
          stage.draw()

          stage.getBatch.begin()
          if (cursorPickUpItem.nonEmpty) {
            val mousePos = game.mousePos()
            val iconPos = if (cursorPickUpItem.get.itemMoveLocation == Inventory) {
              game
                .clientCreature(game.gameplay.gameState)
                .get
                .params
                .inventoryItems(cursorPickUpItem.get.pos)
                .template
                .iconPos
            } else {
              game
                .clientCreature(game.gameplay.gameState)
                .get
                .params
                .equipmentItems(cursorPickUpItem.get.pos)
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
      case None =>
    }
  }

  def update(game: CoreGame): Unit = {
    if (game.clientCreature(game.gameplay.gameState).nonEmpty) {
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
      inventoryItemsActorGroup.items.foreach { case (pos, actor) =>
        val itemIsBeingMoved =
          cursorPickUpItem.nonEmpty && cursorPickUpItem.get.itemMoveLocation == Inventory && cursorPickUpItem.get.pos == pos
        if (inventoryItems.contains(pos) && !itemIsBeingMoved) {
          val iconPos = inventoryItems(pos).template.iconPos
          actor.setDrawable(
            new TextureRegionDrawable(
              new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
            )
          )
        } else {
          actor.setDrawable(null)
        }
      }

      equipmentItemsActorGroup.items.foreach { case (pos, actor) =>
        val itemIsBeingMoved =
          cursorPickUpItem.nonEmpty && cursorPickUpItem.get.itemMoveLocation == Equipment && cursorPickUpItem.get.pos == pos
        if (equipmentItems.contains(pos) && !itemIsBeingMoved) {
          val iconPos = equipmentItems(pos).template.iconPos
          actor.setDrawable(
            new TextureRegionDrawable(
              new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
            )
          )
        } else {
          actor.setDrawable(null)
        }
      }
    }
  }

  def setStageAsInputProcessor(): Unit = Gdx.input.setInputProcessor(stage)

  def actStage(delta: Float): Unit = stage.act(delta)


  def setHoverItemInfoText(hoverText: String): Unit = {
    hoverItemInfo.setText(hoverText)
  }

  def cursorPickUpItem(
                       pos: Int,
                       itemMoveLocation: ItemMoveLocation,
                       game: CoreGame
                     ): Unit = {
    if (cursorPickUpItem.isDefined) {
      game.sendEvent(
        CreatureCursorPickUpItemEvent(
          game.clientCreatureId.get,
          cursorPickUpItem.get.itemMoveLocation,
          cursorPickUpItem.get.pos,
          itemMoveLocation,
          pos
        )
      )
      cursorPickUpItem = None
    } else {
      if (
        game.gameplay.gameState
          .creatures(game.clientCreatureId.get)
          .params
          .inventoryItems
          .contains(pos)
      ) {
        cursorPickUpItem = Some(CursorPickUpItem(itemMoveLocation, pos))
      }
    }
  }
}
