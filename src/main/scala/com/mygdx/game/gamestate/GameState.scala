package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creature: Creature,
    creatures: Map[EntityId[Creature], Creature],
    creatureCounter: Int
) {
  def update(
      creaturePositions: Map[EntityId[Creature], (Float, Float)],
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.each)
      .using { creature =>
        val (x, y) = creaturePositions(creature.params.id)
        creature.update(x, y, delta)
      }
      .pipe(gameState => {
        val enemyCount = gameState.creatures.values
          .count(creature => !creature.params.player && creature.alive)

        if (enemyCount < 3) {
          val nextCreatureId =
            EntityId[Creature]("creature_" + gameState.creatureCounter)

          val newEnemy = Creature.male1(
            nextCreatureId,
            Random.between(2f, 28f),
            Random.between(2f, 18f),
            player = false
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
      Creature.male1(clientInformation.clientCreatureId, 5f, 5f, player = true)

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
