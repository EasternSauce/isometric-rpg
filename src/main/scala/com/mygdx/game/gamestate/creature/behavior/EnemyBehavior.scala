package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

import scala.util.chaining.scalaUtilChainingOps

case class EnemyBehavior() extends CreatureBehavior {
  override def updateMovement(
      creature: Creature,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome.when(creature)(_.params.currentTargetId.isEmpty) {
        creature =>
          lookForNewTarget(creature, gameState)
      }
      creature <- Outcome.when(creature)(_.params.currentTargetId.nonEmpty) {
        creature =>
          pursueTarget(creature, creature.params.currentTargetId.get, gameState)
      }
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

    creature
      .pipe { creature =>
        val distanceToPlayer =
          creature.pos.distance(targetCreature.pos)

        if (distanceToPlayer > creature.params.attackRange) {
          Outcome(
            creature
              .modify(_.params.destination)
              .setTo(targetCreature.pos)
          )
        } else {
          for {
            creature <- Outcome.when(creature)(
              targetCreature.alive && _.attackingAllowed
            )(creature =>
              Outcome(
                creature
                  .modify(_.params.attackAnimationTimer)
                  .using(_.restart())
                  .modify(_.params.attackedCreatureId)
                  .setTo(Some(targetCreature.id))
              )
            )
            creature <- creature.stopMoving()
          } yield creature
        }
      }
  }
}
