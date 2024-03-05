package com.mygdx.game.gamestate.event.physics

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}

case class MakeBodySensorEvent(creatureId: EntityId[Creature])
    extends PhysicsEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
}
