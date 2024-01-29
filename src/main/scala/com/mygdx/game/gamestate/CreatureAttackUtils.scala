package com.mygdx.game.gamestate

import com.mygdx.game.Constants
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

object CreatureAttackUtils {
  def processAttacks: GameState => GameState = { gameState =>
    {
      val creatureAttackEvents = getCreatureAttackEvents(gameState)

      gameState
        .modify(_.creatures.each)
        .using(creature => {
          val totalDamage = creatureAttackEvents
            .filter(_.attackedCreatureId == creature.params.id)
            .map(_.damage)
            .sum

          val attackDone = creatureAttackEvents
            .map(_.attackingCreatureId)
            .contains(creature.params.id)

          creature
            .pipeIf(totalDamage != 0)(_.takeDamage(totalDamage))
            .pipeIf(attackDone)(_.modify(_.params.attackedCreatureId).setTo({
              None
            })) // TODO: should attackedCreatureId be a list or smthng?
        })
    }

  }

  private def getCreatureAttackEvents(
      gameState: GameState
  ): List[CreatureAttackEvent] = {
    gameState.creatures.values.toList
      .filter(creature =>
        creature.params.attackedCreatureId.nonEmpty && creature.params.attackAnimationTimer.isRunning &&
          creature.params.attackAnimationTimer.time > Constants.AttackFrameCount * Constants.AttackFrameDuration * 0.8f
      )
      .map(creature =>
        CreatureAttackEvent(
          creature.params.attackedCreatureId.get,
          creature.params.id,
          creature.params.damage
        )
      )
  }

  private case class CreatureAttackEvent(
      attackedCreatureId: EntityId[Creature],
      attackingCreatureId: EntityId[Creature],
      damage: Float
  )
}
