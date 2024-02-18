package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.mygdx.game.gamestate.creature.{Creature, PrimaryWeaponType}
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
  private var spellcastAnimations: Array[Animation[TextureRegion]] = _
  private var bowAnimations: Array[Animation[TextureRegion]] = _
  private var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    val creature: Creature = gameState.creatures(creatureId)

    standstillAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    attackAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    walkAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    deathAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    textureRegion = Assets.atlas.findRegion(
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
    if (creature.params.animationDefinition.spellcastFrames.nonEmpty) {
      spellcastAnimations = loadAnimations(
        creature.params.animationDefinition.spellcastFrames.get
      )
    }
    if (creature.params.animationDefinition.bowFrames.nonEmpty) {
      bowAnimations = loadAnimations(
        creature.params.animationDefinition.bowFrames.get
      )
    }

    standstillAnimations.foreach(
      _.setPlayMode(Animation.PlayMode.LOOP_PINGPONG)
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
        creature.params.attackAnimationTimer.running && creature.params.attackAnimationTimer.time < creature.params.animationDefinition.attackFrames.totalDuration
      ) {
        if (creature.params.primaryWeaponType == PrimaryWeaponType.Bow) {
          bowAnimations(creature.facingDirection.id)
            .getKeyFrame(creature.params.attackAnimationTimer.time, false)
        } else {
          attackAnimations(creature.facingDirection.id)
            .getKeyFrame(creature.params.attackAnimationTimer.time, false)
        }
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

    val pos = IsometricProjection.translateIsoToScreen(creature.pos)

    batch.draw(
      frame,
      pos.x - Constants.SpriteCenterX,
      pos.y - Constants.SpriteCenterY
    )
  }
}
