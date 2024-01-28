package com.mygdx.game.gamestate

import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creature: Creature,
    creatures: Map[EntityId[Creature], Creature],
    creatureCounter: Int
) {
  def update(
      creaturePositions: Map[EntityId[Creature], Vector2],
      input: Input,
      clientInformation: ClientInformation,
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.each)
      .using { creature =>
        val creaturePos = creaturePositions(creature.params.id)
        creature.update(creaturePos, delta, input, clientInformation, this)
      }
      .pipe(gameState => {
        val enemyCount = gameState.creatures.values
          .count(creature => !creature.params.player && creature.alive)

        if (enemyCount < 3) {
          val nextCreatureId =
            EntityId[Creature]("creature_" + gameState.creatureCounter)

          val newEnemy = Creature.male1(
            nextCreatureId,
            Vector2(Random.between(2f, 28f), Random.between(2f, 18f)),
            player = false,
            baseVelocity = 2f
          )

          gameState
            .modify(_.creatures)
            .using(_.updated(nextCreatureId, newEnemy))
            .modify(_.creatureCounter)
            .using(_ + 1)
        } else {
          gameState
        }
      })
      .pipe(gameState => {
        val creatureAttackEvents = gameState.creatures.values
          .filter(creature =>
            creature.params.attackedCreatureId.nonEmpty && creature.params.attackTimer.isRunning &&
              creature.params.attackTimer.time > Constants.AttackFrameCount * Constants.AttackFrameDuration * 0.8f
          )
          .map(creature =>
            (creature.params.attackedCreatureId.get, creature.params.id)
          )
          .toMap

        var attacksDone: List[EntityId[Creature]] = List()

        gameState
          .modify(_.creatures.each)
          .using(creature => {
            if (creatureAttackEvents.contains(creature.params.id)) {
              val attackerId = creatureAttackEvents(creature.params.id)
              val attacker = gameState.creatures(attackerId)
              attacksDone = attacksDone.appended(attackerId)
              creature.takeDamage(attacker.params.damage)
            } else {
              creature
            }
          })
          .modify(_.creatures.each)
          .using(creature => {
            if (attacksDone.contains(creature.params.id)) {
              creature
                .modify(_.params.attackedCreatureId)
                .setTo(None)
            } else {
              creature
            }
          })
      })
  }

  def getAliveCreatureClosestTo(
      point: Vector2,
      ignored: List[EntityId[Creature]]
  ): Option[Creature] = {
    var closestCreature: Option[Creature] = None

    creatures.values
      .filter(creature =>
        creature.alive && !ignored.contains(creature.params.id)
      )
      .foreach { creature =>
        if (
          closestCreature.isEmpty || closestCreature.get.params.pos.distance(
            point
          ) > creature.params.pos.distance(point)
        ) {
          closestCreature = Some(creature)
        }
      }

    closestCreature
  }
}

object GameState {
  def initialState(clientInformation: ClientInformation): GameState = {
    val creature =
      Creature.male1(
        clientInformation.clientCreatureId,
        Vector2(5f, 5f),
        player = true,
        baseVelocity = 4f
      )

    GameState(
      creature = creature,
      creatures = Map(
        clientInformation.clientCreatureId ->
          creature
      ),
      creatureCounter = 0
    )
  }
}
