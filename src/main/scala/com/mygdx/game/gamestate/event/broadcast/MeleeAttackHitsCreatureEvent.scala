package com.mygdx.game.gamestate.event.broadcast

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{DamageDealingUtils, EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class MeleeAttackHitsCreatureEvent(
    sourceCreatureId: EntityId[Creature],
    targetCreatureId: EntityId[Creature],
    damage: Float
) extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = {
    if (gameState.creatures.contains(targetCreatureId)) {
      gameState
        .modify(_.creatures.at(targetCreatureId))
        .using(creature =>
          creature
            .pipe(
              DamageDealingUtils
                .registerLastAttackedByCreature(sourceCreatureId)
            )
            .pipe(DamageDealingUtils.dealDamageToCreature(damage))
        )
    } else {
      gameState
    }
  }
}
