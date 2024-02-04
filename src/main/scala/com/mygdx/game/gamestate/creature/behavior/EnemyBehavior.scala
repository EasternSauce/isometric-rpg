package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.input.Input
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

import scala.util.chaining.scalaUtilChainingOps

case class EnemyBehavior() extends CreatureBehavior {
  override def updateMovement(
      creature: Creature,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    val enemyAggroed: Option[Creature] =
      gameState.creatures.values.toList.filter(otherCreature =>
        otherCreature.params.player && otherCreature.alive && otherCreature.params.pos
          .distance(creature.params.pos) < Constants.EnemyAggroDistance
      ) match {
        case List() => None
        case creatures =>
          Some(creatures.minBy(_.params.pos.distance(creature.params.pos)))
      }

    enemyAggroed match {
      case Some(otherCreature) =>
        enemyPursuePlayer(creature, otherCreature)
      case _ => creature
    }
  }

  private def enemyPursuePlayer(
      creature: Creature,
      aggroedCreature: Creature
  ): Creature = {
    creature
      .pipe { creature =>
        val distanceToPlayer =
          creature.params.pos.distance(aggroedCreature.params.pos)

        if (distanceToPlayer > creature.params.attackRange) {
          creature
            .modify(_.params.destination)
            .setTo(aggroedCreature.params.pos)
        } else {
          creature
            .pipeIf(creature =>
              aggroedCreature.alive && creature.attackingAllowed
            )(
              _.modify(_.params.attackAnimationTimer)
                .using(_.restart())
                .attack(aggroedCreature)
            )
            .stopMoving()
        }
      }
  }
}
