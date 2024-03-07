package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.broadcast.CreatureAttackAnimationRestartEvent
import com.mygdx.game.gamestate.{EntityId, GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

case class EnemyBehavior() extends CreatureBehavior {
  override def update(
      creature: Creature,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome.when(creature)(_.params.currentTargetId.isEmpty)(
        lookForNewTarget(_, gameState)
      )
      creature <- Outcome.when(creature)(_.params.currentTargetId.nonEmpty) {
        creature =>
          val targetCreature =
            gameState.creatures(creature.params.currentTargetId.get)

          for {
            creature <- pursueTarget(
              creature,
              targetCreature.id,
              gameState
            )
            creature <- Outcome.when(creature)(
              _.pos.distance(targetCreature.pos) < Constants.EnemyLoseAggroRange
            )(creature =>
              Outcome(
                creature.modify(_.params.loseAggroTimer).using(_.restart())
              )
            )
            creature <- Outcome.when(creature)(creature =>
              !targetCreature.alive ||
                (!creature.params.loseAggroTimer.running || creature.params.loseAggroTimer.time > Constants.EnemyLoseAggroTime) &&
                (!creature.params.lastAttackedTimer.running || creature.params.lastAttackedTimer.time > Constants.AttackedByCreatureLoseAggroTime)
            )(loseAggro)
          } yield creature
      }
    } yield creature

  }

  private def loseAggro(creature: Creature): Outcome[Creature] = {
    for {
      creature <- Outcome(creature.modify(_.params.currentTargetId).setTo(None))
      creature <- creature.stopMoving()
    } yield creature
  }

  private def lookForNewTarget(
      creature: Creature,
      gameState: GameState
  ): Outcome[Creature] = {
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
        Outcome(
          creature
            .modify(_.params.currentTargetId)
            .setTo(Some(closestCreature.id))
        )
      case _ => Outcome(creature)
    }
  }

  private def pursueTarget(
      creature: Creature,
      targetCreatureId: EntityId[Creature],
      gameState: GameState
  ): Outcome[Creature] = {
    val targetCreature = gameState.creatures(targetCreatureId)

    val distanceToPlayer =
      creature.pos.distance(targetCreature.pos)

    if (distanceToPlayer > creature.params.attackRange) {
      Outcome(
        creature
          .modify(_.params.destination)
          .setTo(targetCreature.pos)
      )
    } else {
      if (targetCreature.alive && creature.attackAllowed) {
        for {
          creature <- creature.creatureMeleeAttackStart(
            targetCreature.id,
            gameState
          )
          creature <- creature
            .stopMoving()
            .withEvents(List(CreatureAttackAnimationRestartEvent(creature.id)))
        } yield creature
      } else {
        Outcome(creature)
      }

    }

  }
}
