package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.mygdx.game.util.Vector2
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
      clientInformation: ClientInformation,
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.each)
      .using { creature =>
        val creaturePos = creaturePositions(creature.params.id)
        creature.update(creaturePos, delta, clientInformation, this)
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
