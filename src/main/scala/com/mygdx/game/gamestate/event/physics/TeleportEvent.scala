package com.mygdx.game.gamestate.event.physics

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class TeleportEvent(creatureId: EntityId[Creature], pos: Vector2)
    extends PhysicsEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
}
