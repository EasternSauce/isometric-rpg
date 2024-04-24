package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Group, InputEvent}
import com.mygdx.game.{Assets, Constants}

case class InventoryBuilder() extends Group {
  def build(): Group = {
    val inventoryGroup: Group = new Group()

    for {
      x <- 0 until Constants.InventoryWidth
      y <- 0 until Constants.InventoryHeight
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), x, y)

      image.setX(
        Constants.InventoryMargin + Constants.InventoryX + (Constants.InventorySlotSize + 5) * x
      )
      image.setY(
        Constants.InventoryMargin + Constants.InventoryY + (Constants.InventorySlotSize + 5) * (Constants.InventoryHeight - y - 1)
      )
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked slot " + image.slotX + " " + image.slotY)
        }
      })

      inventoryGroup.addActor(image)
    }

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(Assets.atlas.findRegion("inventory_slot"), 0, y)

      image.setX(
        Constants.InventoryMargin + Constants.EquipmentX
      )
      image.setY(
        Constants.InventoryMargin + Constants.EquipmentY + (Constants.InventorySlotSize + 5) * (Constants.EquipmentSlotCount - y - 1)
      )
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked slot " + image.slotX + " " + image.slotY)
        }
      })

      inventoryGroup.addActor(image)
    }

    inventoryGroup
  }

  case class InventorySlotImage(
      atlasRegion: TextureAtlas.AtlasRegion,
      slotX: Int,
      slotY: Int
  ) extends Image(atlasRegion)

}
