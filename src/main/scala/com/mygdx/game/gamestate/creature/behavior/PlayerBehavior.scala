package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input

case class PlayerBehavior() extends CreatureBehavior { // TODO: will this be needed at all? player behavior now comes exclusively from client in form of events
  override def update(
      creature: Creature,
      input: Input,
      gameState: GameState
  ): Outcome[Creature] = {
    Outcome(creature)
  }
}
