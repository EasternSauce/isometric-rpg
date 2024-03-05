package com.mygdx.game.gamestate.event.collision

import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{DamageDealingUtils, EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class AbilityHitsCreatureEvent(
    abilityId: EntityId[Ability],
    creatureId: EntityId[Creature]
) extends CollisionEvent {
  override def applyToGameState(gameState: GameState): GameState = {
    if (
      gameState.abilities
        .contains(abilityId) && gameState.creatures.contains(creatureId)
    ) {
      val ability = gameState.abilities(abilityId)
      val creature = gameState.creatures(creatureId)

      if (ability.params.creatureId != creatureId) {
        gameState
          .modify(_.creatures.at(creatureId))
          .using(creature =>
            creature
              .pipe(
                DamageDealingUtils.registerLastAttackedByCreature(
                  ability.params.creatureId
                )
              )
              .pipe(
                DamageDealingUtils.dealDamageToCreature(ability.params.damage)
              )
          )
          .modify(_.abilities)
          .usingIf(creature.alive && ability.destroyedOnContact)(
            _.removed(abilityId)
          )
      } else {
        gameState
      }
    } else {
      gameState
    }

  }
}
