package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.ability.{Ability, AbilityParams, Arrow}
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.ModifyPimp

case class CreatureShootArrowEvent(
    sourceCreatureId: EntityId[Creature],
    arrowDirection: Vector2,
    damage: Float
) extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = {

    val abilityId: EntityId[Ability] =
      EntityId("ability" + gameState.abilityCounter)

    val sourceCreature = gameState.creatures(sourceCreatureId)

    gameState
      .modify(_.abilities)
      .using(
        _.updated(
          abilityId,
          Arrow(
            AbilityParams(
              abilityId,
              sourceCreatureId,
              sourceCreature.params.pos,
              arrowDirection,
              damage
            )
          )
        )
      )
      .modify(_.abilityCounter)
      .using(_ + 1)
  }
}
