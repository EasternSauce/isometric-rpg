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
    Outcome.when(creature)(_.params.currentTarget.isEmpty) { creature =>
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
              .modify(_.params.currentTarget)
              .setTo(Some(closestCreature.id))
          )
        case _ => Outcome(creature)
      }
    }

  }

  private def enemyPursuePlayer( // TODO: do this every loop if target nonempty + add lose target logic
      creature: Creature,
      pursuedCreature: Creature
  ): Outcome[Creature] = {
    creature
      .pipe { creature =>
        val distanceToPlayer =
          creature.pos.distance(pursuedCreature.pos)

        if (distanceToPlayer > creature.params.attackRange) {
          Outcome(
            creature
              .modify(_.params.destination)
              .setTo(pursuedCreature.pos)
          )
        } else {
          for {
            creature <- Outcome.when(creature)(
              pursuedCreature.alive && _.attackingAllowed
            )(creature =>
              Outcome(
                creature
                  .modify(_.params.attackAnimationTimer)
                  .using(_.restart())
                  .modify(_.params.attackedCreatureId)
                  .setTo(Some(pursuedCreature.id))
              )
            )
            creature <- creature.stopMoving()
          } yield creature
        }
      }
  }
}
