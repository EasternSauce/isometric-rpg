package com.mygdx.game.view

import com.mygdx.game.gamestate.GameState
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureRenderer(creatureId: String) extends Renderable {
  private val animations: Map[CreatureAnimationType, CreatureAnimation] = {
    def entry(
        creatureAnimationType: CreatureAnimationType,
        creatureId: String
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
