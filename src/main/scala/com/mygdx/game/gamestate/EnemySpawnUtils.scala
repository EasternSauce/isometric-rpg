package com.mygdx.game.gamestate

import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.ModifyPimp

import scala.util.Random

object EnemySpawnUtils {
  def processSpawns: GameState => GameState = { gameState =>
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

  }
}
