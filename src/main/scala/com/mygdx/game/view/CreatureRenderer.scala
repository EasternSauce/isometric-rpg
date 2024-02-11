package com.mygdx.game.view

import com.badlogic.gdx.graphics.Color
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.{Rectangle, Vector2}
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureRenderer(creatureId: EntityId[Creature]) extends Renderable {
  private var animations: Map[CreatureAnimationType, CreatureAnimation] = _

  def init(gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    animations = {
      def entry(
          creatureAnimationType: CreatureAnimationType,
          creatureId: EntityId[Creature]
      ): (CreatureAnimationType, CreatureAnimation) = {
        creatureAnimationType -> CreatureAnimation(
          creatureId,
          creatureAnimationType
        )
      }

      creature.params.textureNames.keys.map(entry(_, creatureId)).toMap
    }

    animations.values.foreach(_.init(gameState))
  }

  override def pos(gameState: GameState): Vector2 = {
    val creature = gameState.creatures(creatureId)

    creature.params.pos
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    animations.values.foreach(_.render(batch, gameState))
  }

  def renderLifeBar(spriteBatch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    if (!creature.params.deathAcknowledged) {
      val lifeBarWidth = 32f
      val currentLifeBarWidth =
        lifeBarWidth * creature.params.life / creature.params.maxLife

      val creatureScreenPos =
        IsometricProjection.translateIsoToScreen(creature.params.pos)

      val barPos = Vector2(
        creatureScreenPos.x - lifeBarWidth / 2f,
        creatureScreenPos.y + 32f
      )

      renderBar(spriteBatch, barPos, lifeBarWidth, Color.ORANGE)
      renderBar(spriteBatch, barPos, currentLifeBarWidth, Color.RED)
    }
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
