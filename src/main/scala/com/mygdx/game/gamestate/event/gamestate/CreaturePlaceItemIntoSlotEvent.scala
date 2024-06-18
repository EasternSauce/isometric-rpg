package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.EquipmentSlotType
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.item.Item
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.view.inventorywindow.ItemMoveLocation
import com.mygdx.game.view.inventorywindow.ItemMoveLocation.{Inventory, ItemMoveLocation}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreaturePlaceItemIntoSlotEvent(
    creatureId: EntityId[Creature],
    sourceLocation: ItemMoveLocation,
    sourcePos: Int,
    destinationLocation: ItemMoveLocation,
    destinationPos: Int
) extends GameStateEvent {
  def applyToGameState(gameState: GameState): GameState = {
    val sourceItem = getLocationItems(sourceLocation, gameState)(sourcePos)
    val destinationItem =
      getLocationItems(destinationLocation, gameState).get(destinationPos)

    moveItem(sourceItem, destinationItem, gameState)
  }

  private def moveItem(
      sourceItem: Item,
      destinationItem: Option[Item],
      gameState: GameState
  ): GameState = {
    if (sourceLocation == Inventory) {
      if (destinationLocation == Inventory) {
        moveInventoryToInventory(
          sourcePos,
          sourceItem,
          destinationPos,
          destinationItem,
          gameState
        )
      } else {
        moveInventoryToEquipment(
          sourcePos,
          sourceItem,
          destinationPos,
          destinationItem,
          gameState
        )
      }
    } else {
      if (destinationLocation == Inventory) {
        moveEquipmentToInventory(
          sourcePos,
          sourceItem,
          destinationPos,
          destinationItem,
          gameState
        )
      } else {
        moveEquipmentToEquipment(
          sourcePos,
          sourceItem,
          destinationPos,
          destinationItem,
          gameState
        )
      }
    }
  }

  private def getLocationItems(
      location: ItemMoveLocation,
      gameState: GameState
  ): Map[Int, Item] = {
    if (location == ItemMoveLocation.Inventory) {
      gameState.creatures(creatureId).params.inventoryItems
    } else {
      gameState.creatures(creatureId).params.equipmentItems
    }
  }

  private def moveEquipmentToEquipment(
      sourcePos: Int,
      sourceItem: Item,
      destinationPos: Int,
      destinationItem: Option[Item],
      gameState: GameState
  ): GameState = {
    val moveAllowed: Boolean = sourceItem.template.equipmentSlotType.contains(
      EquipmentSlotType(destinationPos)
    )

    if (moveAllowed) {
      if (destinationItem.nonEmpty) {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature =>
            creature
              .modify(_.params.equipmentItems)
              .using(_.updated(destinationPos, sourceItem))
              .modify(_.params.equipmentItems)
              .using(_.updated(sourcePos, destinationItem.get))
          )
      } else {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature =>
            creature
              .modify(_.params.equipmentItems)
              .using(_.updated(destinationPos, sourceItem))
              .modify(_.params.equipmentItems)
              .using(_.removed(sourcePos))
          )
      }
    } else {
      gameState
    }
  }

  private def moveEquipmentToInventory(
      sourcePos: Int,
      sourceItem: Item,
      destinationPos: Int,
      destinationItem: Option[Item],
      gameState: GameState
  ): GameState = {
    if (destinationItem.nonEmpty) {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(destinationPos, sourceItem))
            .modify(_.params.equipmentItems)
            .using(_.updated(sourcePos, destinationItem.get))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(destinationPos, sourceItem))
            .modify(_.params.equipmentItems)
            .using(_.removed(sourcePos))
        )
    }
  }

  private def moveInventoryToEquipment(
      sourcePos: Int,
      sourceItem: Item,
      destinationPos: Int,
      destinationItem: Option[Item],
      gameState: GameState
  ): GameState = {
    val moveAllowed: Boolean = sourceItem.template.equipmentSlotType.contains(
      EquipmentSlotType(destinationPos)
    )

    if (moveAllowed) {
      if (destinationItem.nonEmpty) {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature =>
            creature
              .modify(_.params.equipmentItems)
              .using(_.updated(destinationPos, sourceItem))
              .modify(_.params.inventoryItems)
              .using(_.updated(sourcePos, destinationItem.get))
          )
      } else {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature =>
            creature
              .modify(_.params.equipmentItems)
              .using(_.updated(destinationPos, sourceItem))
              .modify(_.params.inventoryItems)
              .using(_.removed(sourcePos))
          )
      }
    } else {
      gameState
    }
  }

  private def moveInventoryToInventory(
      sourcePos: Int,
      sourceItem: Item,
      destinationPos: Int,
      destinationItem: Option[Item],
      gameState: GameState
  ): GameState = {
    if (destinationItem.nonEmpty) {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(destinationPos, sourceItem))
            .modify(_.params.inventoryItems)
            .using(_.updated(sourcePos, destinationItem.get))
        )
    } else {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creature =>
          creature
            .modify(_.params.inventoryItems)
            .using(_.updated(destinationPos, sourceItem))
            .modify(_.params.inventoryItems)
            .using(_.removed(sourcePos))
        )
    }
  }
}
