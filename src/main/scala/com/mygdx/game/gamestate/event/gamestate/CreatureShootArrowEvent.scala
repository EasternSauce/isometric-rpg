package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.ability.{Ability, AbilityParams, Arrow}
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.softwaremill.quicklens.ModifyPimp

case class CreatureShootArrowEvent(
    sourceCreatureId: EntityId[Creature],
    damage: Float
) extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = {

    val abilityId: EntityId[Ability] =
      EntityId("ability" + gameState.abilityCounter)

    val creature = gameState.creatures(sourceCreatureId)

    gameState
      .modify(_.abilities)
      .using(
        _.updated(
          abilityId,
          Arrow(
            AbilityParams(
              abilityId,
              creature.params.currentAreaId,
              sourceCreatureId,
              creature.params.pos,
              creature.params.facingVector,
              damage
            )
          )
        )
      )
      .modify(_.abilityCounter)
      .using(_ + 1)
  }
}
