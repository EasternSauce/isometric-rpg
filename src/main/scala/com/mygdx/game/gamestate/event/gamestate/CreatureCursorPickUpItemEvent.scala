package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.view.inventory.ItemMoveLocation.{Inventory, ItemMoveLocation}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureCursorPickUpItemEvent(
    creatureId: EntityId[Creature],
    fromLocation: ItemMoveLocation,
    fromPos: Int,
    toLocation: ItemMoveLocation,
    toPos: Int
) extends GameStateEvent {
  def applyToGameState(gameState: GameState): GameState = {
    if (fromLocation == Inventory) {
      if (
        gameState.creatures(creatureId).params.inventoryItems.contains(fromPos)
      ) {
        val itemSource =
          gameState.creatures(creatureId).params.inventoryItems(fromPos)

        if (toLocation == Inventory) {
          if (
            gameState
              .creatures(creatureId)
              .params
              .inventoryItems
              .contains(toPos)
          ) {
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
        } else {
          gameState
            .modify(_.creatures.at(creatureId))
            .using(creature => creature)
        }
      } else {
        gameState
      }
    } else {
      if (toLocation == Inventory) {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature => creature)
      } else {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature => creature)
      }
    }
  }

}
