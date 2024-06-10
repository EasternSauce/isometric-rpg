package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.{Image, Window}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group}
import com.mygdx.game.core.CoreGame

case class InventoryActorGroup() {
  protected var slotsGroup: Actor = _
  protected var itemsGroup: Actor = _

  private var inventorySlots: InventorySlots = _
  private var inventoryItems: InventoryItems = _

  def init(game: CoreGame): Unit = {
    this.inventorySlots = InventorySlots()
    inventorySlots.init(game)

    this.inventoryItems = InventoryItems()
    inventoryItems.init(game)

    this.slotsGroup = createSlotsGroup()
    this.itemsGroup = createItemsGroup()
  }

  private def createItemsGroup(): Actor = {
    val inventoryItemsGroup: Group = new Group()
    inventoryItems.items.values.foreach(inventoryItemsGroup.addActor(_))
    inventoryItemsGroup
  }

  def createSlotsGroup(): Actor = {
    val inventorySlotsGroup: Group = new Group()
    inventorySlots.slots.values.foreach(inventorySlotsGroup.addActor(_))
    inventorySlotsGroup
  }

  def slots: Map[Int, Image] = inventorySlots.slots
  def items: Map[Int, Image] = inventoryItems.items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsGroup)
    window.addActor(itemsGroup)
  }
}
