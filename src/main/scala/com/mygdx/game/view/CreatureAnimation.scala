package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType
import com.mygdx.game.{Assets, Constants, FramesDefinition}

case class CreatureAnimation(
    creatureId: EntityId[Creature],
    creatureAnimationType: CreatureAnimationType
) {
  private var standstillAnimations: Array[Animation[TextureRegion]] = _
  private var attackAnimations: Array[Animation[TextureRegion]] = _
  private var walkAnimations: Array[Animation[TextureRegion]] = _
  private var deathAnimations: Array[Animation[TextureRegion]] = _
  private var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    standstillAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    attackAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    walkAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    deathAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    val creature = gameState.creatures(creatureId)

    textureRegion = Assets.atlas.get.findRegion(
      creature.params.textureNames(creatureAnimationType)
    )

    standstillAnimations = loadAnimations(
      creature.params.animationDefinition.stanceFrames
    )
    attackAnimations = loadAnimations(
      creature.params.animationDefinition.attackFrames
    )
    walkAnimations = loadAnimations(
      creature.params.animationDefinition.walkFrames
    )
    deathAnimations = loadAnimations(
      creature.params.animationDefinition.deathFrames
    )

  }

  private def loadAnimations(
      stanceFrames: FramesDefinition
  ): Array[Animation[TextureRegion]] = {
    for {
      i <- (0 until WorldDirection.values.size).toArray
    } yield {
      val standstillFrames =
        for {
          j <-
            (stanceFrames.start until stanceFrames.start + stanceFrames.count).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.SpriteTextureWidth,
          i * Constants.SpriteTextureHeight,
          Constants.SpriteTextureWidth,
          Constants.SpriteTextureHeight
        )

      new Animation[TextureRegion](
        stanceFrames.frameDuration,
        standstillFrames: _*
      )
    }
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    val frame =
      if (
        creature.params.attackAnimationTimer.isRunning && creature.params.attackAnimationTimer.time < creature.params.animationDefinition.attackFrames.totalDuration
      ) {
        attackAnimations(creature.facingDirection.id)
          .getKeyFrame(creature.params.attackAnimationTimer.time, false)
      } else if (creature.moving) {
        walkAnimations(creature.facingDirection.id)
          .getKeyFrame(creature.params.animationTimer.time, true)
      } else if (creature.alive) {
        standstillAnimations(creature.facingDirection.id)
          .getKeyFrame(creature.params.animationTimer.time, true)
      } else {
        deathAnimations(creature.facingDirection.id)
          .getKeyFrame(creature.params.deathAnimationTimer.time, false)
      }

    val pos = IsometricProjection.translateIsoToScreen(creature.params.pos)

    batch.draw(
      frame,
      pos.x - Constants.SpriteCenterX,
      pos.y - Constants.SpriteCenterY
    )
  }
}
