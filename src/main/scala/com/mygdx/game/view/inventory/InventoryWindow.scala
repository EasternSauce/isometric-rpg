package com.mygdx.game.view.inventory

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.{TextArea, TextButton, TextField, Window}
import com.badlogic.gdx.scenes.scene2d.utils.{ClickListener, TextureRegionDrawable}
import com.badlogic.gdx.utils.Align
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.event.gamestate.PlayerToggleInventoryEvent
import com.mygdx.game.view.inventory.ItemMoveLocation._
import com.mygdx.game.{Assets, Constants}

case class InventoryWindow() {

  private var window: Window = _

  private var inventory: InventoryActorGroup = _
  private var equipment: EquipmentActorGroup = _

  private var hoverItemInfo: TextField = _

  def init(stage: InventoryStage, game: CoreGame): Unit = {
    window = new Window("Inventory", game.scene2dSkin)

    window.setX(800)
    window.setY(200)
    window.setWidth(800)
    window.setHeight(600)

    window.setKeepWithinStage(true)

    hoverItemInfo = createHoverItemInfoTextField(game)
    window.addActor(hoverItemInfo)

    val exitButton: TextButton = createExitButton(game)

    window.addActor(exitButton)

    inventory = InventoryActorGroup()
    equipment = EquipmentActorGroup()

    inventory.init(game)
    equipment.init(game)

    inventory.addToWindow(window)
    equipment.addToWindow(window)

    stage.addActor(window)
  }

  private def createExitButton(game: CoreGame) = {
    val exitButton = new TextButton("Exit", game.scene2dSkin)
    exitButton.setX(650)
    exitButton.setY(10)
    exitButton.setWidth(120)
    exitButton.setHeight(30)

    exitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        game.sendEvent(PlayerToggleInventoryEvent(game.clientCreatureId.get))
      }
    })
    exitButton
  }

  private def createHoverItemInfoTextField(game: CoreGame): TextField = {
    val hoverItemInfo: TextField = new TextArea("", game.scene2dSkin)
    hoverItemInfo.setX(Constants.hoverItemInfoX)
    hoverItemInfo.setY(Constants.hoverItemInfoY)
    hoverItemInfo.setWidth(Constants.hoverItemInfoWidth)
    hoverItemInfo.setHeight(Constants.hoverItemInfoHeight)
    hoverItemInfo.setAlignment(Align.topLeft)
    hoverItemInfo.setTouchable(null)

    hoverItemInfo
  }

  def update(
      itemCursorPickupState: Option[ItemCursorPickupState],
      game: CoreGame
  ): Unit = {
    val inventoryItems = game
      .clientCreature(game.gameState)
      .get
      .params
      .inventoryItems

    val equipmentItems = game
      .clientCreature(game.gameState)
      .get
      .params
      .equipmentItems

    inventory.items.foreach { case (pos, actor) =>
      val itemIsBeingMoved =
        itemCursorPickupState.nonEmpty && itemCursorPickupState.get.itemMoveLocation == Inventory && itemCursorPickupState.get.pos == pos
      if (inventoryItems.contains(pos) && !itemIsBeingMoved) {
        val iconPos = inventoryItems(pos).template.iconPos
        actor.setDrawable(
          new TextureRegionDrawable(
            new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
          )
        )
      } else {
        actor.setDrawable(null)
      }
    }

    equipment.items.foreach { case (pos, actor) =>
      val itemIsBeingMoved =
        itemCursorPickupState.nonEmpty && itemCursorPickupState.get.itemMoveLocation == Equipment && itemCursorPickupState.get.pos == pos
      if (equipmentItems.contains(pos) && !itemIsBeingMoved) {
        val iconPos = equipmentItems(pos).template.iconPos
        actor.setDrawable(
          new TextureRegionDrawable(
            new TextureRegionDrawable(Assets.getIcon(iconPos.x, iconPos.y))
          )
        )
      } else {
        actor.setDrawable(null)
      }
    }
  }

  def setHoverItemInfoText(hoverText: String): Unit =
    hoverItemInfo.setText(hoverText)

}
