package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

object CreaturesFinderUtils {
  def getAliveCreatureClosestTo(
      point: Vector2,
      ignored: List[EntityId[Creature]],
      gameState: GameState
  ): Option[Creature] = {
    var closestCreature: Option[Creature] = None

    gameState.creatures.values
      .filter(creature => creature.alive && !ignored.contains(creature.id))
      .foreach { creature =>
        if (
          closestCreature.isEmpty || closestCreature.get.pos.distance(
            point
          ) > creature.pos.distance(point)
        ) {
          closestCreature = Some(creature)
        }
      }

    closestCreature
  }
}
