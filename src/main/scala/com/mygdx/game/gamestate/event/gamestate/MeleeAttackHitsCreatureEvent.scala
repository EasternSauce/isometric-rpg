package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{DamageDealingUtils, EntityId, GameState}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class MeleeAttackHitsCreatureEvent(
    sourceCreatureId: EntityId[Creature],
    destinationCreatureId: EntityId[Creature],
    damage: Float
) extends GameStateEvent {
  override def applyToGameState(gameState: GameState): GameState = gameState
    .modify(_.creatures.at(destinationCreatureId))
    .using(creature =>
      creature
        .pipe(
          DamageDealingUtils.registerLastAttackedByCreature(sourceCreatureId)
        )
        .pipe(DamageDealingUtils.dealDamageToCreature(damage))
    )

}
