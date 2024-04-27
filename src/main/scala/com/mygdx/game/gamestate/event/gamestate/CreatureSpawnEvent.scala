package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.softwaremill.quicklens.ModifyPimp

case class CreatureSpawnEvent(creature: Creature) extends GameStateEvent { // TODO: do we need to pass entire creature?
  override def applyToGameState(gameState: GameState): GameState = {
    gameState
      .modify(_.creatures)
      .using(_.updated(creature.id, creature))
      .modify(_.creatureCounter)
      .using(_ + 1)
      .modify(_.activeCreatureIds)
      .using(_ + creature.id)
  }
}
