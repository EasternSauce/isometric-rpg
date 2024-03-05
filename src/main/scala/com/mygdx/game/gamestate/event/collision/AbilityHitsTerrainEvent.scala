package com.mygdx.game.gamestate.event.collision

import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.ModifyPimp

case class AbilityHitsTerrainEvent(
    abilityId: EntityId[Ability],
    terrainId: String
) extends CollisionEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
    .modify(_.abilities)
    .using(_.removed(abilityId))
}
