package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.mygdx.game.core.CoreGame
import com.mygdx.game.view.StageActor
import com.mygdx.game.{Assets, Constants}

case class InventorySlotsActor() extends StageActor {
  private var _slots: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    val inventorySlotsGroup: Group = new Group()

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

      _slots = _slots.updated(count, image)

      count = count + 1

      inventorySlotsGroup.addActor(image)
    }

    this._actor = inventorySlotsGroup
  }

  def slots: Map[Int, Image] = _slots

}
