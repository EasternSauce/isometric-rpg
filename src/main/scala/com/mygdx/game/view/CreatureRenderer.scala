package com.mygdx.game.view

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.util.{Rectangle, Vector2}
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

  override def pos(gameState: GameState): Vector2 = {
    val creature = gameState.creatures(creatureId)

    creature.params.pos
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    CreatureAnimationType.values.foreach(creatureAnimationType => {
      animations(creatureAnimationType).render(batch, gameState)
    })
  }

  def renderLifeBar(spriteBatch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)
    val lifeBarWidth = 32f
    val currentLifeBarWidth =
      lifeBarWidth * creature.params.life / creature.params.maxLife

    val creatureScreenPos =
      IsometricProjection.translateIsoToScreen(creature.params.pos)

    val barPos = Vector2(
      creatureScreenPos.x - lifeBarWidth / 2f,
      creatureScreenPos.y + 60f
    )

    renderBar(spriteBatch, barPos, lifeBarWidth, Color.ORANGE)
    renderBar(spriteBatch, barPos, currentLifeBarWidth, Color.RED)
  }

  private def renderBar(
      spriteBatch: SpriteBatch,
      barPos: Vector2,
      lifeBarWidth: Float,
      color: Color
  ): Unit = {
    val lifeBarHeight = 3f
    spriteBatch.filledRectangle(
      Rectangle(barPos.x, barPos.y, lifeBarWidth, lifeBarHeight),
      color
    )
  }
}
