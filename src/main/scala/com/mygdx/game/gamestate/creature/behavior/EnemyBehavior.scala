package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState, Outcome}
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.softwaremill.quicklens.ModifyPimp

case class EnemyBehavior() {
  def update(
      creature: Creature,
      gameState: GameState
  ): Outcome[Creature] = {
    val res = if (creature.params.currentTargetId.isEmpty) {
      lookForNewTarget(creature, gameState)
    } else {
      val targetCreature =
        gameState.creatures(creature.params.currentTargetId.get)

      pursueTarget(
        creature,
        targetCreature.id,
        gameState
      ).pipeIf(
        _.pos.distance(targetCreature.pos) < Constants.EnemyLoseAggroRange
      )(_.modify(_.params.loseAggroTimer).using(_.restart()))
        .pipeIf(creature => canLoseAggro(creature, targetCreature))(loseAggro)
    }

    Outcome(res)
  }

  private def canLoseAggro(
      creature: Creature,
      targetCreature: Creature
  ): Boolean = {
    !targetCreature.alive ||
    (!creature.params.loseAggroTimer.running ||
      creature.params.loseAggroTimer.time > Constants.EnemyLoseAggroTime) &&
    (!creature.params.lastAttackedTimer.running ||
      creature.params.lastAttackedTimer.time > Constants.AttackedByCreatureLoseAggroTime)
  }

  private def loseAggro(creature: Creature): Creature = {
    creature
      .modify(_.params.currentTargetId)
      .setTo(None)
      .modify(_.params.destination)
      .setTo(creature.pos)
  }

  private def lookForNewTarget(
      creature: Creature,
      gameState: GameState
  ): Creature = {
    val maybeClosestCreature: Option[Creature] =
      gameState.creatures.values.toList.filter(otherCreature =>
        otherCreature.params.player && otherCreature.alive && otherCreature.pos
          .distance(creature.pos) < Constants.EnemyAggroDistance
      ) match {
        case List() => None
        case creatures =>
          Some(creatures.minBy(_.pos.distance(creature.pos)))
      }

    maybeClosestCreature match {
      case Some(closestCreature) =>
        creature
          .modify(_.params.currentTargetId)
          .setTo(Some(closestCreature.id))
      case _ => creature
    }
  }

  private def pursueTarget(
      creature: Creature,
      targetCreatureId: EntityId[Creature],
      gameState: GameState
  ): Creature = {
    val targetCreature = gameState.creatures(targetCreatureId)

    val distanceToPlayer =
      creature.pos.distance(targetCreature.pos)

    if (distanceToPlayer > creature.params.attackRange) {
      creature
        .modify(_.params.destination)
        .setTo(targetCreature.pos)
        .modify(_.params.facingVector)
        .setTo(creature.pos.vectorTowards(targetCreature.pos))

    } else {
      if (targetCreature.alive && creature.attackingAllowed) {
        meleeAttackStart(
          creature.id,
          targetCreature.id,
          gameState
        )
          .modify(_.params.destination)
          .setTo(creature.pos)
          .modify(_.params.attackAnimationTimer)
          .using(_.restart())
          .modify(_.params.facingVector)
          .setTo(creature.pos.vectorTowards(targetCreature.pos))
      } else {
        creature
      }

    }

  }

  private def meleeAttackStart(
      creatureId: EntityId[Creature],
      otherCreatureId: EntityId[Creature],
      gameState: GameState
  ): Creature = {
    val creature = gameState.creatures(creatureId)
    val otherCreature = gameState.creatures(otherCreatureId)

    if (
      otherCreature.pos
        .distance(creature.pos) < creature.params.attackRange
    ) {
      creature
        .modify(_.params.attackedCreatureId)
        .setTo(Some(otherCreature.id))
        .modify(_.params.attackPending)
        .setTo(true)
    } else {
      creature
    }
  }
}
