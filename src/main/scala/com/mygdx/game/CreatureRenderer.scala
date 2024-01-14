package com.mygdx.game

import com.badlogic.gdx.graphics.g2d.{Animation, Sprite, TextureAtlas, TextureRegion}
import com.mygdx.game.WorldDirection.WorldDirection

case class CreatureRenderer(creatureId: String) {
  var sprite: Sprite = _

  var facingTextures: Array[TextureRegion] = _
  var runningAnimations: Array[Animation[TextureRegion]] = _

  var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    sprite = new Sprite()

    facingTextures = new Array[TextureRegion](4)

    runningAnimations = new Array[Animation[TextureRegion]](4)

    val creature = gameState.creatures(creatureId)

    textureRegion = Assets.atlas.get.findRegion(creature.textureName)

    for (i <- 0 until 4)
      facingTextures(i) = new TextureRegion(
        textureRegion,
        creature.neutralStanceFrame * Constants.TileTextureWidth,
        i * Constants.TileTextureHeight,
        Constants.TileTextureWidth,
        Constants.TileTextureHeight
      )

    for (i <- 0 until 4) {
      val frames =
        for {j <- (0 until creature.frameCount).toArray} yield new TextureRegion(
          textureRegion,
          j * Constants.TileTextureWidth,
          i * Constants.TileTextureHeight,
          Constants.TileTextureWidth,
          Constants.TileTextureHeight
        )
      runningAnimations(i) = new Animation[TextureRegion](creature.frameDuration, frames: _*)
    }
  }

  def runningAnimationFrame(currentDirection: WorldDirection, gameState: GameState): TextureRegion = {
    val creature = gameState.creatures(creatureId)

    runningAnimations(creature.dirMap(currentDirection))
      .getKeyFrame(creature.animationTimer.time, true)
  }

  def facingTextureFrame(currentDirection: WorldDirection, gameState: GameState): TextureRegion = {
    val creature = gameState.creatures(creatureId)

    facingTextures(creature.dirMap(currentDirection))
  }


}
