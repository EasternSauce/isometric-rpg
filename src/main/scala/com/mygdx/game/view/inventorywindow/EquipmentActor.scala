package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.ui.{Image, Window}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group}
import com.mygdx.game.core.CoreGame

case class EquipmentActor() {
  private var slotsActor: Actor = _
  private var itemsActor: Actor = _

  private var equipmentSlots: EquipmentSlots = _
  private var equipmentItems: EquipmentItems = _

  def init(game: CoreGame): Unit = {
    this.equipmentSlots = EquipmentSlots()
    equipmentSlots.init(game)

    this.equipmentItems = EquipmentItems()
    equipmentItems.init(game)

    this.slotsActor = createSlotsActor()
    this.itemsActor = createItemsActor()
  }

  private def createSlotsActor(): Actor = {
    val equipmentSlotsGroup: Group = new Group()
    equipmentSlots.slots.values.foreach(equipmentSlotsGroup.addActor(_))
    equipmentSlots.slotTexts.values.foreach(equipmentSlotsGroup.addActor(_))
    equipmentSlotsGroup
  }

  private def createItemsActor(): Actor = {
    val equipmentItemsGroup: Group = new Group()
    equipmentItems.items.values.foreach(equipmentItemsGroup.addActor(_))
    equipmentItemsGroup
  }

  def slots: Map[Int, Image] = equipmentSlots.slots
  def items: Map[Int, Image] = equipmentItems.items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsActor)
    window.addActor(itemsActor)
  }

}
