package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.input.Input

trait CreatureBehavior {
  def updateMovement(
      creature: Creature,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature
}
