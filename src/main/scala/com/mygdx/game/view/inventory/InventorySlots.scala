package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.{Assets, Constants}

case class InventorySlots() {
  var slots: Map[Int, Image] = Map()

  def init(): Unit = {
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

      slots = slots.updated(count, image)

      count = count + 1

    }
  }
}
