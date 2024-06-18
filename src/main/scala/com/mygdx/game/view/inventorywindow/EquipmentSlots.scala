package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.ui.{Image, TextField}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.{Assets, Constants, EquipmentSlotType}

case class EquipmentSlots() {
  var slots: Map[Int, Image] = Map()
  var slotTexts: Map[Int, TextField] = Map()

  def init(game: CoreGame): Unit = {
    var counter: Int = 0

    val x = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val slot: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), counter)

      slot.setX(Constants.equipmentSlotPositionX(x))
      slot.setY(Constants.equipmentSlotPositionY(y))
      slot.setWidth(Constants.InventorySlotSize)
      slot.setHeight(Constants.InventorySlotSize)

      slots = slots.updated(counter, slot)

      val text: TextField =
        new TextField(EquipmentSlotType(counter).displayName, game.scene2dSkin)

      text.setX(Constants.equipmentSlotPositionX(x) - 120)
      text.setY(Constants.equipmentSlotPositionY(y) + 10)
      text.setWidth(110)
      text.setAlignment(Align.center)
      text.setTouchable(null)

      slotTexts = slotTexts.updated(counter, text)

      counter = counter + 1
    }

  }
}
