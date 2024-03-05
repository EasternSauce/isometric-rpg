package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

case class CreatureSpawnEvent(creature: Creature) extends BroadcastEvent {
  override def applyToGameState(gameState: GameState): GameState = {
    gameState
      .modify(_.creatures)
      .using(_.updated(creature.id, creature))
      .modify(_.creatureCounter)
      .using(_ + 1)
  }
}
