package com.mygdx.game.view

import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureRenderer(creatureId: EntityId[Creature]) extends Renderable {
  private val animations: Map[CreatureAnimationType, CreatureAnimation] = {
    def entry(
        creatureAnimationType: CreatureAnimationType,
        creatureId: EntityId[Creature]
    ): (CreatureAnimationType, CreatureAnimation) = {
      creatureAnimationType -> CreatureAnimation(
        creatureId,
        creatureAnimationType
      )
    }
    CreatureAnimationType.values.toList.map(entry(_, creatureId)).toMap
  }

  def init(gameState: GameState): Unit = {
    CreatureAnimationType.values.foreach(creatureAnimationType => {
      animations(creatureAnimationType).init(gameState)
    })
  }

  override def pos(gameState: GameState): (Float, Float) = {
    val creature = gameState.creatures(creatureId)

    (creature.params.x, creature.params.y)
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    CreatureAnimationType.values.foreach(creatureAnimationType => {
      animations(creatureAnimationType).render(batch, gameState)
    })
  }
}
