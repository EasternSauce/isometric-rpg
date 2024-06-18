package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.core.CoreGame
import com.mygdx.game.{Assets, Constants}

case class InventorySlots() {
  var slots: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    var counter: Int = 0

    for {
      y <- 0 until Constants.InventoryHeight
      x <- 0 until Constants.InventoryWidth
    } {
      val slot: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), counter)

      slot.setX(Constants.inventorySlotPositionX(x))
      slot.setY(Constants.inventorySlotPositionY(y))
      slot.setWidth(Constants.InventorySlotSize)
      slot.setHeight(Constants.InventorySlotSize)

      slots = slots.updated(counter, slot)

      counter = counter + 1

    }
  }
}
