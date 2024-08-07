package com.mygdx.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.creature.{Creature, PrimaryWeaponType, SecondaryWeaponType}
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

      creature.params.texturePaths.keys.map(entry(_, creatureId)).toMap
    }

    animations.values.foreach(_.init(gameState))
  }

  override def pos(gameState: GameState): Vector2 = {
    val creature = gameState.creatures(creatureId)

    creature.pos
  }

  override def areaId(gameState: GameState): AreaId = {
    val creature = gameState.creatures(creatureId)

    creature.params.currentAreaId
  }

  override def render(
      batch: SpriteBatch,
      worldCameraPos: Vector2,
      gameState: GameState
  ): Unit = {
    val creature = gameState.creatures(creatureId)

    if (creature.invisible) {
      animations(CreatureAnimationType.Body).render(batch, gameState)
      if (!creature.params.renderBodyOnly) {
        animations(CreatureAnimationType.Head).render(batch, gameState)
      }
      if (creature.params.primaryWeaponType != PrimaryWeaponType.None) {
        animations(CreatureAnimationType.Weapon).render(batch, gameState)
      }
      if (creature.params.secondaryWeaponType == SecondaryWeaponType.Shield) {
        animations(CreatureAnimationType.Shield).render(batch, gameState)
      }
    }
  }

  def renderLifeBar(spriteBatch: SpriteBatch, gameState: GameState): Unit = {
    if (gameState.creatures.contains(creatureId)) {
      val creature = gameState.creatures(creatureId)

      if (!creature.params.deathAcknowledged) {
        val lifeBarWidth = 32f
        val currentLifeBarWidth =
          lifeBarWidth * creature.params.life / creature.params.maxLife

        val creatureScreenPos =
          IsometricProjection.translatePosIsoToScreen(creature.pos)

        val barPos = Vector2(
          creatureScreenPos.x - lifeBarWidth / 2f,
          creatureScreenPos.y + 48f
        )

        renderBar(spriteBatch, barPos, lifeBarWidth, Color.ORANGE)
        renderBar(spriteBatch, barPos, currentLifeBarWidth, Color.RED)
      }
    }
  }

  def renderPlayerName(
      spriteBatch: SpriteBatch,
      font: BitmapFont,
      gameState: GameState
  ): Unit = {
    if (
      gameState.creatures
        .contains(creatureId) && gameState.creatures(creatureId).params.player
    ) {
      val creature = gameState.creatures(creatureId)

      if (!creature.params.deathAcknowledged) {
        val creatureScreenPos =
          IsometricProjection.translatePosIsoToScreen(creature.pos)

        val namePos = Vector2(
          creatureScreenPos.x - 25f,
          creatureScreenPos.y + 70
        )

        spriteBatch.drawFont(font, creature.id.value, namePos)
      }
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

  override def renderPriority(gameState: GameState): Boolean = {
    val creature = gameState.creatures(creatureId)

    creature.alive
  }
}
