package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input

trait CreatureBehavior {
  def update(
      creature: Creature,
      input: Input,
      gameState: GameState
  ): Outcome[Creature]
}
