package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Group, InputEvent}
import com.mygdx.game.view.StageActor
import com.mygdx.game.{Assets, Constants}

case class EquipmentSlotsActor() extends StageActor {
  private var _slots: Map[Int, Image] = Map()

  def init(): Unit = {
    val equipmentSlotsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), 0, y)

      image.setX(Constants.equipmentSlotPositionX(0))
      image.setY(Constants.equipmentSlotPositionY(y))
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

      equipmentSlotsGroup.addActor(image)
    }

    this._actor = equipmentSlotsGroup
  }

  def slots: Map[Int, Image] = _slots

}
