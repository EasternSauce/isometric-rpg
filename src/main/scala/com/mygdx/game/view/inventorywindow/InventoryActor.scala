package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.ui.{Image, Window}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group}
import com.mygdx.game.core.CoreGame

case class InventoryActor() {
  private var slotsActor: Actor = _
  private var itemsActor: Actor = _

  private var inventorySlots: InventorySlots = _
  private var inventoryItems: InventoryItems = _

  def init(game: CoreGame): Unit = {
    this.inventorySlots = InventorySlots()
    inventorySlots.init(game)

    this.inventoryItems = InventoryItems()
    inventoryItems.init(game)

    this.slotsActor = createSlotsActor()
    this.itemsActor = createItemsActor()
  }

  private def createItemsActor(): Actor = {
    val inventoryItemsGroup: Group = new Group()
    inventoryItems.items.values.foreach(inventoryItemsGroup.addActor(_))
    inventoryItemsGroup
  }

  private def createSlotsActor(): Actor = {
    val inventorySlotsGroup: Group = new Group()
    inventorySlots.slots.values.foreach(inventorySlotsGroup.addActor(_))
    inventorySlotsGroup
  }

  def slots: Map[Int, Image] = inventorySlots.slots
  def items: Map[Int, Image] = inventoryItems.items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsActor)
    window.addActor(itemsActor)
  }
}
