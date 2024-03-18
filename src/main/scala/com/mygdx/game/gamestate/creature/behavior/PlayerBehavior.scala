package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.creature.{Creature, CreaturesFinderUtils, PrimaryWeaponType}
import com.mygdx.game.gamestate.event.broadcast.{CreatureGoToEvent, CreaturePlayAttackAnimationEvent, CreatureStopMovingEvent}
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.ModifyPimp

case class PlayerBehavior() extends CreatureBehavior {
  override def update(
      creature: Creature,
      input: Input,
      gameState: GameState
  ): Outcome[Creature] = {
    val mouseWorldPos: Vector2 = input.mouseWorldPos(creature.pos)

    for {
      creature <- Outcome.when(creature)(_.alive)(
        moveTowardsTarget(input, mouseWorldPos)
      )
      creature <- Outcome.when(creature)(creature =>
        creature.alive && input.attackButtonJustPressed && creature.attackAllowed
      )(performAttackClick(mouseWorldPos, gameState))
    } yield creature
  }

  private[creature] def moveTowardsTarget(
      input: Input,
      mouseWorldPos: Vector2
  ): Creature => Outcome[Creature] = { creature =>
    if (input.moveButtonPressed) {
      Outcome[Creature](
        creature
          .modify(_.params.attackAnimationTimer)
          .usingIf(creature.params.attackAnimationTimer.running)(_.stop())
      ).withEvents {
        if (
          creature.params.destination.distance(
            mouseWorldPos
          ) > Constants.MinimumDistanceBetweenDestinations
        ) {
          List(CreatureGoToEvent(creature.id, mouseWorldPos))
        } else {
          List()
        }
      }
    } else {
      Outcome(
        creature
      )
    }
  }

  private def performAttackClick(
      mouseWorldPos: Vector2,
      gameState: GameState
  ): Creature => Outcome[Creature] = { creature =>
    val maybeClosestCreatureId =
      CreaturesFinderUtils.getAliveCreatureIdClosestTo(
        mouseWorldPos,
        List(creature.id),
        gameState
      )

    for {
      creature <-
        if (creature.params.primaryWeaponType == PrimaryWeaponType.Bow)
          creature
            .creatureRangedAttackStart()
            .withEvents(
              List(
                CreaturePlayAttackAnimationEvent(
                  creature.id,
                  creature.pos.vectorTowards(mouseWorldPos)
                )
              )
            )
        else {
          if (maybeClosestCreatureId.nonEmpty) {
            val closestCreaturePos =
              gameState.creatures(maybeClosestCreatureId.get).pos

            creature
              .creatureMeleeAttackStart(
                maybeClosestCreatureId.get,
                gameState
              )
              .withEvents(
                List(
                  CreaturePlayAttackAnimationEvent(
                    creature.id,
                    creature.pos.vectorTowards(closestCreaturePos)
                  )
                )
              )
          } else {
            Outcome(creature).withEvents(
              List(
                CreaturePlayAttackAnimationEvent(
                  creature.id,
                  creature.pos.vectorTowards(mouseWorldPos)
                )
              )
            )
          }
        }
      creature <- Outcome(creature).withEvents(
        List(CreatureStopMovingEvent(creature.id))
      )
    } yield creature
  }
}
