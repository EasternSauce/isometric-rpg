package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
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
        creature.update(
          delta = delta,
          newPos = creaturePositions(creature.params.id),
          input = input,
          clientInformation = clientInformation,
          gameState = this
        )
      }
      .pipe(EnemySpawnUtils.processSpawns)
      .pipe(CreatureAttackUtils.processAttacks)
  }
}

object GameState {
  def initialState(clientInformation: ClientInformation): GameState = {
    val player =
      Creature.male1(
        clientInformation.clientCreatureId,
        Vector2(5f, 5f),
        player = true,
        baseVelocity = 4f
      )

    GameState(
      creatures = Map(
        clientInformation.clientCreatureId ->
          player
      ),
      creatureCounter = 0
    )
  }
}
