package com.mygdx.game.gamestate.event.collision

import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.ModifyPimp

case class AbilityHitsTerrainGameStateEvent(
    abilityId: EntityId[Ability],
    terrainId: String
) extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
    .modify(_.abilities)
    .using(_.removed(abilityId))
}
