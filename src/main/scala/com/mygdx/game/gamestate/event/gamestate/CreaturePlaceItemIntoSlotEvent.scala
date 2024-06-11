package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.view.inventory.ItemMoveLocation.{Inventory, ItemMoveLocation}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreaturePlaceItemIntoSlotEvent(
    creatureId: EntityId[Creature],
    fromLocation: ItemMoveLocation,
    fromPos: Int,
    toLocation: ItemMoveLocation,
    toPos: Int
) extends GameStateEvent {
  def applyToGameState(gameState: GameState): GameState = {
    println("running event")
    if (fromLocation == Inventory) {
      if (toLocation == Inventory) {
        moveInventoryToInventory(gameState)
      } else {
        moveInventoryToEquipment(gameState)
      }
    } else {
      if (toLocation == Inventory) {
        moveEquipmentToInventory(gameState)
      } else {
        moveEquipmentToEquipment(gameState)
      }
    }
  }

  private def moveEquipmentToEquipment(gameState: GameState): GameState = {
    val itemSource =
      gameState.creatures(creatureId).params.equipmentItems(fromPos)

    val destinationItemExists = gameState
      .creatures(creatureId)
      .params
      .equipmentItems
      .contains(toPos)

    if (destinationItemExists) {
      val itemDestination =
        gameState.creatures(creatureId).params.equipmentItems(toPos)

      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.equipmentItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.equipmentItems)
            .using(_.updated(fromPos, itemDestination))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.equipmentItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.equipmentItems)
            .using(_.removed(fromPos))
        )
    }
  }

  private def moveEquipmentToInventory(gameState: GameState): GameState = {
    val itemSource =
      gameState.creatures(creatureId).params.equipmentItems(fromPos)

    val destinationItemExists = gameState
      .creatures(creatureId)
      .params
      .inventoryItems
      .contains(toPos)

    if (destinationItemExists) {
      val itemDestination =
        gameState.creatures(creatureId).params.inventoryItems(toPos)

      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.equipmentItems)
            .using(_.updated(fromPos, itemDestination))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.equipmentItems)
            .using(_.removed(fromPos))
        )
    }
  }

  private def moveInventoryToEquipment(gameState: GameState): GameState = {
    val itemSource =
      gameState.creatures(creatureId).params.inventoryItems(fromPos)

    val destinationItemExists = gameState
      .creatures(creatureId)
      .params
      .equipmentItems
      .contains(toPos)

    if (destinationItemExists) {
      val itemDestination =
        gameState.creatures(creatureId).params.equipmentItems(toPos)

      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.equipmentItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.inventoryItems)
            .using(_.updated(fromPos, itemDestination))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.equipmentItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.inventoryItems)
            .using(_.removed(fromPos))
        )
    }
  }

  private def moveInventoryToInventory(gameState: GameState): GameState = {
    val itemSource =
      gameState.creatures(creatureId).params.inventoryItems(fromPos)

    val destinationItemExists = gameState
      .creatures(creatureId)
      .params
      .inventoryItems
      .contains(toPos)

    if (destinationItemExists) {
      val itemDestination =
        gameState.creatures(creatureId).params.inventoryItems(toPos)

      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.inventoryItems)
            .using(_.updated(fromPos, itemDestination))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(toPos, itemSource))
            .modify(_.params.inventoryItems)
            .using(_.removed(fromPos))
        )
    }
  }
}
