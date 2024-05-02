package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.{Image, TextField, Window}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.{Assets, Constants}

case class EquipmentActorGroup() {
  protected var slotsGroup: Actor = _
  protected var itemsGroup: Actor = _

  private var _slots: Map[Int, Image] = Map()
  private var _items: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    this.slotsGroup = createSlotsGroup(game)
    this.itemsGroup = createItemsGroup(game)
  }

  def createSlotsGroup(game: CoreGame): Actor = {
    val equipmentSlotsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), count)

      image.setX(Constants.equipmentSlotPositionX(0))
      image.setY(Constants.equipmentSlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      _slots = _slots.updated(count, image)

      val text: TextField =
        new TextField(Constants.equipmentSlotNames(count), game.scene2dSkin)

      text.setX(Constants.equipmentSlotPositionX(0) - 120)
      text.setY(Constants.equipmentSlotPositionY(y) + 10)
      text.setWidth(110)
      text.setAlignment(Align.center)
      text.setTouchable(null)

      count = count + 1

      equipmentSlotsGroup.addActor(image)
      equipmentSlotsGroup.addActor(text)
    }

    equipmentSlotsGroup
  }

  def createItemsGroup(game: CoreGame): Actor = {
    val equipmentItemsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(null, count)

      image.setX(Constants.equipmentSlotPositionX(0))
      image.setY(Constants.equipmentSlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked item")

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
            clientCreature.nonEmpty && clientCreature.get.params.equipmentItems
              .contains(image.pos)
          ) {
            val itemInfo =
              clientCreature.get.params.equipmentItems(image.pos).info
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

      equipmentItemsGroup.addActor(image)
    }

    equipmentItemsGroup
  }

  def slots: Map[Int, Image] = _slots
  def items: Map[Int, Image] = _items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsGroup)
    window.addActor(itemsGroup)
  }

}
