package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent}
import com.mygdx.game.Constants
import com.mygdx.game.core.CoreGame
import com.mygdx.game.view.StageActor

case class EquipmentItemsActorGroup() extends StageActor {
  private var _items: Map[Int, Image] = Map()

  def init(game: CoreGame): Unit = {
    val equipmentItemsGroup: Group = new Group()

    var count: Int = 0

    for {
      y <- 0 until Constants.EquipmentSlotCount
    } {
      val image: InventorySlotImage =
        InventorySlotImage(null, count)

      image.setX(Constants.equipmentSlotPositionX(0))
      image.setY(Constants.equipmentSlotPositionY(y))
      image.setWidth(Constants.InventorySlotSize)
      image.setHeight(Constants.InventorySlotSize)

      image.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          println("clicked item")


        }

        override def enter(
            event: InputEvent,
            x: Float,
            y: Float,
            pointer: Int,
            fromActor: Actor
        ): Unit = {
          val image = event.getTarget.asInstanceOf[InventorySlotImage]
          val clientCreature = game.clientCreature(game.gameplay.gameState)
          if (
            clientCreature.nonEmpty && clientCreature.get.params.equipmentItems
              .contains(image.pos)
          ) {
            val itemInfo =
              clientCreature.get.params.equipmentItems(image.pos).info
            game.gameplay.view.setInventoryHoverItemInfoText(itemInfo)
          }
        }

        override def exit(
            event: InputEvent,
            x: Float,
            y: Float,
            pointer: Int,
            toActor: Actor
        ): Unit = {
          game.gameplay.view.setInventoryHoverItemInfoText("")
        }
      })

      _items = _items.updated(count, image)

      count = count + 1

      equipmentItemsGroup.addActor(image)
    }

    this._actor = equipmentItemsGroup
  }

  def items: Map[Int, Image] = _items

}
