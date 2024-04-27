package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.{Image, TextField}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.view.StageActor
import com.mygdx.game.{Assets, Constants}

case class EquipmentSlotsActor() extends StageActor {
  private var _slots: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    val equipmentSlotsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), count)

      image.setX(Constants.equipmentSlotPositionX(0))
      image.setY(Constants.equipmentSlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      _slots = _slots.updated(count, image)

      val text: TextField =
        new TextField(Constants.equipmentSlotNames(count), game.scene2dSkin)

      text.setX(Constants.equipmentSlotPositionX(0) - 120)
      text.setY(Constants.equipmentSlotPositionY(y) + 10)
      text.setWidth(110)
      text.setAlignment(Align.center)
      text.setTouchable(null)

      count = count + 1

      equipmentSlotsGroup.addActor(image)
      equipmentSlotsGroup.addActor(text)
    }

    this._actor = equipmentSlotsGroup
  }

  def slots: Map[Int, Image] = _slots

}
