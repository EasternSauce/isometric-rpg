package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.{Image, Window}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent}
import com.mygdx.game.core.CoreGame
import com.mygdx.game.{Assets, Constants}

case class InventoryActorGroup() {
  protected var slotsGroup: Actor = _
  protected var itemsGroup: Actor = _

  private var _slots: Map[Int, Image] = Map()
  private var _items: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    createSlotsGroup(game)

    this.slotsGroup = createSlotsGroup(game)
    this.itemsGroup = createItemsGroup(game)
  }

  private def createItemsGroup(game: CoreGame): Actor = {
    val inventoryItemsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.InventoryHeight
      x <- 0 until Constants.InventoryWidth
    } {
      val image: InventorySlotImage =
        InventorySlotImage(null, count)

      image.setX(Constants.inventorySlotPositionX(x))
      image.setY(Constants.inventorySlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked item")

          game.gameplay.view.inventoryCursorPickUpItem(
            image.pos,
            ItemMoveLocation.Inventory,
            game
          )
        }

        override def enter(
            event: InputEvent,
            x: Float,
            y: Float,
            pointer: Int,
            fromActor: Actor
        ): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          val clientCreature = game.clientCreature(game.gameplay.gameState)
          if (
            clientCreature.nonEmpty && clientCreature.get.params.inventoryItems
              .contains(image.pos)
          ) {
            val itemInfo =
              clientCreature.get.params.inventoryItems(image.pos).info
            game.gameplay.view.setInventoryHoverItemInfoText(itemInfo)
          }
        }

        override def exit(
            event: InputEvent,
            x: Float,
            y: Float,
            pointer: Int,
            toActor: Actor
        ): Unit = {
          game.gameplay.view.setInventoryHoverItemInfoText("")
        }
      })

      _items = _items.updated(count, image)

      count = count + 1

      inventoryItemsGroup.addActor(image)
    }

    inventoryItemsGroup
  }

  def createSlotsGroup(game: CoreGame): Actor = {
    val inventorySlotsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.InventoryHeight
      x <- 0 until Constants.InventoryWidth
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), count)

      image.setX(Constants.inventorySlotPositionX(x))
      image.setY(Constants.inventorySlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      _slots = _slots.updated(count, image)

      count = count + 1

      inventorySlotsGroup.addActor(image)
    }

    inventorySlotsGroup
  }

  def slots: Map[Int, Image] = _slots
  def items: Map[Int, Image] = _items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsGroup)
    window.addActor(itemsGroup)
  }
}
