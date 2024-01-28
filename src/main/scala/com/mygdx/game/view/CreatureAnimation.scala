package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType
import com.mygdx.game.{Assets, Constants}

case class CreatureAnimation(
    creatureId: EntityId[Creature],
    creatureAnimationType: CreatureAnimationType
) {
//  private var facingTextures: Array[TextureRegion] = _
  private var standstillAnimations: Array[Animation[TextureRegion]] = _
  private var attackAnimations: Array[Animation[TextureRegion]] = _
  private var walkAnimations: Array[Animation[TextureRegion]] = _
  private var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    standstillAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    attackAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    walkAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    val creature = gameState.creatures(creatureId)

    textureRegion = Assets.atlas.get.findRegion(
      creature.params.textureNames(creatureAnimationType)
    )

    for (i <- 0 until WorldDirection.values.size) {
      val standstillFrames =
        for {
          j <-
            (Constants.StandstillFrameStart until Constants.StandstillFrameStart + Constants.StandStillFrameCount).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.SpriteTextureWidth,
          i * Constants.SpriteTextureHeight,
          Constants.SpriteTextureWidth,
          Constants.SpriteTextureHeight
        )

      standstillAnimations(i) = new Animation[TextureRegion](
        Constants.StandstillFrameDuration,
        standstillFrames: _*
      )

      val idleFrames =
        for {
          j <-
            (Constants.AttackFrameStart until Constants.AttackFrameStart + Constants.AttackFrameCount).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.SpriteTextureWidth,
          i * Constants.SpriteTextureHeight,
          Constants.SpriteTextureWidth,
          Constants.SpriteTextureHeight
        )

      attackAnimations(i) = new Animation[TextureRegion](
        Constants.AttackFrameDuration,
        idleFrames: _*
      )

      val runningFrames =
        for {
          j <-
            (Constants.WalkFrameStart until Constants.WalkFrameStart + Constants.WalkFrameCount).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.SpriteTextureWidth,
          i * Constants.SpriteTextureHeight,
          Constants.SpriteTextureWidth,
          Constants.SpriteTextureHeight
        )
      walkAnimations(i) = new Animation[TextureRegion](
        Constants.WalkFrameDuration,
        runningFrames: _*
      )
    }
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    val frame = if (
      creature.params.attackTimer.isRunning && creature.params.attackTimer.time < Constants.AttackFrameCount * Constants.AttackFrameDuration
    ) {
      attackAnimations(creature.facingDirection.id)
        .getKeyFrame(creature.params.attackTimer.time, false)
    } else if (creature.moving) {
      walkAnimations(creature.facingDirection.id)
        .getKeyFrame(creature.params.animationTimer.time, true)
    } else {
      standstillAnimations(creature.facingDirection.id)
        .getKeyFrame(creature.params.animationTimer.time, true)
    }

    val pos = IsometricProjection.translateIsoToScreen(creature.params.pos)

    batch.draw(
      frame,
      pos.x - Constants.SpriteCenterX,
      pos.y - Constants.SpriteCenterY
    )
  }
}
