package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.{Creature, CreaturesFinderUtils, PrimaryWeaponType}
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureAttackEvent(
    creatureId: EntityId[Creature],
    destination: Vector2
) extends GameStateEvent {

  override def applyToGameState(gameState: GameState): GameState = {
    if (
      gameState.creatures.contains(creatureId) &&
      gameState.creatures(creatureId).alive &&
      gameState.creatures(creatureId).attackingAllowed
    ) {
      gameState
        .modify(_.creatures.at(creatureId))
        .using(creatureAttack(destination, gameState))
    } else {
      gameState
    }
  }

  private def creatureAttack(
      destination: Vector2,
      gameState: GameState
  ): Creature => Creature = { creature =>
    (if (creature.params.primaryWeaponType == PrimaryWeaponType.Bow) {
       performRangedAttack(creature, destination)
     } else {
       performMeleeAttack(creature, destination, gameState)
     })
      .modify(_.params.destination)
      .setTo(creature.params.pos)
  }

  private def performMeleeAttack(
      creature: Creature,
      destination: Vector2,
      gameState: GameState
  ): Creature = {
    val maybeClosestCreatureId =
      CreaturesFinderUtils.getAliveCreatureIdClosestTo(
        destination,
        List(creature.id),
        gameState
      )

    if (maybeClosestCreatureId.nonEmpty) {
      meleeAttackTarget(creature, gameState, maybeClosestCreatureId)
    } else {
      meleeSwingAndMiss(creature, destination)
    }
  }

  private def meleeSwingAndMiss(
      creature: Creature,
      destination: Vector2
  ): Creature = {
    playAttackAnimation(creature, destination)
  }

  private def playAttackAnimation(
      creature: Creature,
      destination: Vector2
  ): Creature = {
    creature
      .modify(_.params.attackAnimationTimer)
      .using(_.restart())
      .modify(_.params.facingVector)
      .setTo(creature.pos.vectorTowards(destination))
  }

  private def meleeAttackTarget(
      creature: Creature,
      gameState: GameState,
      maybeClosestCreatureId: Option[EntityId[Creature]]
  ): Creature = {
    val closestCreature =
      gameState.creatures(maybeClosestCreatureId.get)

    playAttackAnimation(creature, closestCreature.pos)
      .pipeIf(_ =>
        closestCreature.pos
          .distance(creature.pos) < creature.params.attackRange
      )(
        _.modify(_.params.attackedCreatureId)
          .setTo(Some(closestCreature.id))
          .modify(_.params.attackPending)
          .setTo(true)
      )
  }

  private def performRangedAttack(
      creature: Creature,
      destination: Vector2
  ): Creature = {
    val vectorTowardsDestination =
      creature.pos.vectorTowards(destination)

    creature
      .modify(_.params.attackAnimationTimer)
      .using(_.restart())
      .modify(_.params.facingVector)
      .setTo(vectorTowardsDestination)
      .modify(_.params.attackPending)
      .setTo(true)
  }
}
