package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.Constants
import com.mygdx.game.core.CoreGame

case class EquipmentItems() {

  var items: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    var counter: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val item: InventorySlotImage =
        InventorySlotImage(null, counter)

      item.setX(Constants.equipmentSlotPositionX(0))
      item.setY(Constants.equipmentSlotPositionY(y))
      item.setWidth(Constants.InventorySlotSize)
      item.setHeight(Constants.InventorySlotSize)

      item.addListener(EquipmentItemClickListener(game))

      items = items.updated(counter, item)

      counter = counter + 1
    }
  }
}
