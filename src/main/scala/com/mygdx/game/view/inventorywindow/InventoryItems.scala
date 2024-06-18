package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.Constants
import com.mygdx.game.core.CoreGame

case class InventoryItems() {

  var items: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    var counter: Int = 0

    for {
      y <- 0 until Constants.InventoryHeight
      x <- 0 until Constants.InventoryWidth
    } {
      val item: InventorySlotImage =
        InventorySlotImage(null, counter)

      item.setX(Constants.inventorySlotPositionX(x))
      item.setY(Constants.inventorySlotPositionY(y))
      item.setWidth(Constants.InventorySlotSize)
      item.setHeight(Constants.InventorySlotSize)

      item.addListener(InventoryItemClickListener(game))

      items = items.updated(counter, item)

      counter = counter + 1
    }
  }
}
