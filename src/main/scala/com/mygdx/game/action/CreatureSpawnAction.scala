package com.mygdx.game.action

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

case class CreatureSpawnAction(creature: Creature) extends GameStateAction {
  override def applyToGameState(gameState: GameState): GameState = {
    gameState
      .modify(_.creatures)
      .using(_.updated(creature.id, creature))
      .modify(_.creatureCounter)
      .using(_ + 1)
  }
}
