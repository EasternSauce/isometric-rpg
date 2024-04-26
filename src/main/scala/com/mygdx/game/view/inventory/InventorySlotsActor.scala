package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Group, InputEvent}
import com.mygdx.game.view.StageActor
import com.mygdx.game.{Assets, Constants}

case class InventorySlotsActor() extends StageActor {
  private var _slots: Map[Int, Image] = Map()

  def init(): Unit = {
    val inventorySlotsGroup: Group = new Group()

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

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked slot " + image.slotX + " " + image.slotY)
        }
      })

      _slots = _slots.updated(count, image)

      count = count + 1

      inventorySlotsGroup.addActor(image)
    }

    this._actor = inventorySlotsGroup
  }

  def slots: Map[Int, Image] = _slots

}
