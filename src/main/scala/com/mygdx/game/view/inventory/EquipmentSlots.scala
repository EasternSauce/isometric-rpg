package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.{Image, TextField}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.{Assets, Constants}

case class EquipmentSlots() {
  var slots: Map[Int, Image] = Map()
  var slotTexts: Map[Int, TextField] = Map()

  def init(game: CoreGame): Unit = {
    var counter: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val slot: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), counter)

      slot.setX(Constants.equipmentSlotPositionX(0))
      slot.setY(Constants.equipmentSlotPositionY(y))
      slot.setWidth(Constants.InventorySlotSize)
      slot.setHeight(Constants.InventorySlotSize)

      slots = slots.updated(counter, slot)

      val text: TextField =
        new TextField(Constants.equipmentSlotNames(counter), game.scene2dSkin)

      text.setX(Constants.equipmentSlotPositionX(0) - 120)
      text.setY(Constants.equipmentSlotPositionY(y) + 10)
      text.setWidth(110)
      text.setAlignment(Align.center)
      text.setTouchable(null)

      slotTexts = slotTexts.updated(counter, text)

      counter = counter + 1
    }

  }
}
