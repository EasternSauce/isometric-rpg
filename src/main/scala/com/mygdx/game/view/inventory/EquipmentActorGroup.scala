package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.{Image, Window}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group}
import com.mygdx.game.core.CoreGame

case class EquipmentActorGroup() {
  protected var slotsGroup: Actor = _
  protected var itemsGroup: Actor = _

  private var equipmentSlots: EquipmentSlots = _
  private var equipmentItems: EquipmentItems = _

  def init(game: CoreGame): Unit = {
    this.equipmentSlots = EquipmentSlots()
    equipmentSlots.init(game)

    this.equipmentItems = EquipmentItems()
    equipmentItems.init(game)

    this.slotsGroup = createSlotsGroup()
    this.itemsGroup = createItemsGroup()
  }

  def createSlotsGroup(): Actor = {
    val equipmentSlotsGroup: Group = new Group()
    equipmentSlots.slots.values.foreach(equipmentSlotsGroup.addActor(_))
    equipmentSlots.slotTexts.values.foreach(equipmentSlotsGroup.addActor(_))
    equipmentSlotsGroup
  }

  def createItemsGroup(): Actor = {
    val equipmentItemsGroup: Group = new Group()
    equipmentItems.items.values.foreach(equipmentItemsGroup.addActor(_))
    equipmentItemsGroup
  }

  def slots: Map[Int, Image] = equipmentSlots.slots
  def items: Map[Int, Image] = equipmentItems.items

  def addToWindow(window: Window): Unit = {
    window.addActor(slotsGroup)
    window.addActor(itemsGroup)
  }

}
