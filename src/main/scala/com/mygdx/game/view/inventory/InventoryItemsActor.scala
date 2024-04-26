package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.{Assets, Constants}
import com.mygdx.game.view.StageActor

case class InventoryItemsActor() extends StageActor {
  private var _items: Map[Int, Image] = Map()

  def init(): Unit = {
    val inventoryItemsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.InventoryHeight
      x <- 0 until Constants.InventoryWidth
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), x, y)

      image.setX(Constants.inventorySlotPositionX(x))
      image.setY(Constants.inventorySlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      _items = _items.updated(count, image)

      count = count + 1

      inventoryItemsGroup.addActor(image)
    }

    this._actor = inventoryItemsGroup
  }

  def items: Map[Int, Image] = _items

}
