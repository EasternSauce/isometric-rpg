package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.WorldDirection
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType
import com.mygdx.game.{Assets, Constants}

case class CreatureAnimation(
    creatureId: String,
    creatureAnimationType: CreatureAnimationType
) {
  private var facingTextures: Array[TextureRegion] = _
  private var runningAnimations: Array[Animation[TextureRegion]] = _
  private var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    facingTextures = new Array[TextureRegion](WorldDirection.values.size)
    runningAnimations =
      new Array[Animation[TextureRegion]](WorldDirection.values.size)

    val creature = gameState.creatures(creatureId)

    textureRegion = Assets.atlas.get.findRegion(
      creature.params.textureNames(creatureAnimationType)
    )

    for (i <- 0 until WorldDirection.values.size)
      facingTextures(i) = new TextureRegion(
        textureRegion,
        creature.params.neutralStanceFrame * Constants.SpriteTextureWidth,
        i * Constants.SpriteTextureHeight,
        Constants.SpriteTextureWidth,
        Constants.SpriteTextureHeight
      )

    for (i <- 0 until WorldDirection.values.size) {
      val frames =
        for {
          j <-
            (Constants.WalkingFrameStart until Constants.WalkingFrameStart + Constants.WalkingFrameCount).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.SpriteTextureWidth,
          i * Constants.SpriteTextureHeight,
          Constants.SpriteTextureWidth,
          Constants.SpriteTextureHeight
        )
      runningAnimations(i) =
        new Animation[TextureRegion](Constants.WalkingFrameDuration, frames: _*)
    }
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    val frame = if (creature.moving) {
      runningAnimations(creature.facingDirection.id)
        .getKeyFrame(creature.params.animationTimer.time, true)
    } else {
      facingTextures(creature.facingDirection.id)
    }

    val (x, y) =
      IsometricProjection.translateIsoToScreen(
        creature.params.x,
        creature.params.y
      )

    batch.draw(frame, x - Constants.SpriteCenterX, y - Constants.SpriteCenterY)
  }
}
