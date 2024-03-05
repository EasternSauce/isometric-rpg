package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event.broadcast.CreatureSpawnEvent
import com.mygdx.game.util.Vector2

import scala.util.Random

object EnemySpawnUtils {
  def handleEnemySpawns: GameState => Outcome[GameState] = { gameState =>
    val enemyCount = gameState.creatures.values
      .count(creature => !creature.params.player && creature.alive)

    if (enemyCount < 3) {
      val nextCreatureId =
        EntityId[Creature]("creature_" + gameState.creatureCounter)

      val newEnemy = CreatureFactory.rat(
        nextCreatureId,
        Vector2(Random.between(2f, 28f), Random.between(2f, 18f)),
        player = false,
        baseSpeed = 2f
      )

      Outcome(gameState).withEvents(List(CreatureSpawnEvent(newEnemy)))
    } else {
      Outcome(gameState)
    }
  }
}
