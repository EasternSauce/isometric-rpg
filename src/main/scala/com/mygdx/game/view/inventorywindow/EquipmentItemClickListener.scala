package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent}
import com.mygdx.game.core.CoreGame

case class EquipmentItemClickListener(game: CoreGame) extends ClickListener {
  override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
    val image = event.getTarget.asInstanceOf[InventorySlotImage]

    game.gameplay.view.onSlotClick(
      image.pos,
      ItemMoveLocation.Equipment,
      game
    )
  }

  override def enter(
      event: InputEvent,
      x: Float,
      y: Float,
      pointer: Int,
      fromActor: Actor
  ): Unit = {
    val image = event.getTarget.asInstanceOf[InventorySlotImage]
    val clientCreature = game.clientCreature(game.gameState)
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
}
