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
    enemyPursuePlayer(creature, clientInformation, gameState)
  }

  private def enemyPursuePlayer(
      creature: Creature,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    val player = gameState.creatures(clientInformation.clientCreatureId)

    creature
      .pipe { creature =>
        val distanceToPlayer = creature.params.pos.distance(player.params.pos)

        if (distanceToPlayer > Constants.EnemyAttackDistance) {
          creature
            .modify(_.params.destination)
            .setTo(player.params.pos)
        } else {
          creature
            .pipeIf(creature => player.alive && creature.attackingAllowed)(
              _.modify(_.params.attackAnimationTimer)
                .using(_.restart())
                .attack(player)
            )
            .stopMoving()
        }
      }
  }
}
