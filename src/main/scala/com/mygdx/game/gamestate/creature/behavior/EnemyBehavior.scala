package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{GameState, Outcome}
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
    val enemyAggroed: Option[Creature] =
      gameState.creatures.values.toList.filter(otherCreature =>
        otherCreature.params.player && otherCreature.alive && otherCreature.pos
          .distance(creature.pos) < Constants.EnemyAggroDistance
      ) match {
        case List() => None
        case creatures =>
          Some(creatures.minBy(_.pos.distance(creature.pos)))
      }

    enemyAggroed match {
      case Some(otherCreature) =>
        enemyPursuePlayer(creature, otherCreature)
      case _ => Outcome(creature)
    }
  }

  private def enemyPursuePlayer(
      creature: Creature,
      aggroedCreature: Creature
  ): Outcome[Creature] = {
    creature
      .pipe { creature =>
        val distanceToPlayer =
          creature.pos.distance(aggroedCreature.pos)

        if (distanceToPlayer > creature.params.attackRange) {
          Outcome(
            creature
              .modify(_.params.destination)
              .setTo(aggroedCreature.pos)
          )
        } else {
          for {
            creature <- Outcome.when(creature)(
              aggroedCreature.alive && _.attackingAllowed
            )(creature =>
              Outcome(
                creature
                  .modify(_.params.attackAnimationTimer)
                  .using(_.restart())
                  .modify(_.params.attackedCreatureId)
                  .setTo(Some(aggroedCreature.id))
              )
            )
            creature <- creature.stopMoving()
          } yield creature
        }
      }
  }
}
