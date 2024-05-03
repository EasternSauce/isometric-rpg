package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event.gamestate.CreatureSpawnEvent
import com.mygdx.game.util.Vector2

import scala.util.Random

object EnemySpawnUtils {
  def handleEnemySpawns: GameState => Outcome[GameState] = { gameState =>
    val enemyCount = gameState.creatures.values
      .count(creature => !creature.params.player && creature.alive)

    if (enemyCount < 80) {
      val nextCreatureId =
        EntityId[Creature]("creature_" + gameState.creatureCounter)

      val rand = Math.random()

      val newEnemy = if (rand > 0.5f) {
        CreatureFactory.rat(
          nextCreatureId,
          Vector2(Random.between(2f, 198f), Random.between(2f, 198f)),
          player = false,
          baseSpeed = 4f
        )
      } else if (rand > 0.2f) {
        CreatureFactory.zombie(
          nextCreatureId,
          Vector2(Random.between(2f, 198f), Random.between(2f, 198f)),
          player = false,
          baseSpeed = 4f
        )
      } else {
        CreatureFactory.wyvern(
          nextCreatureId,
          Vector2(Random.between(2f, 198f), Random.between(2f, 198f)),
          player = false,
          baseSpeed = 4f
        )
      }

      Outcome(gameState).withBroadcastEvents(List(CreatureSpawnEvent(newEnemy)))
    } else {
      Outcome(gameState)
    }
  }
}
