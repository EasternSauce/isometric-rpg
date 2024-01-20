package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.{Animation, Sprite, TextureRegion}
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.screen.SpriteBatch
import com.mygdx.game.view.tile.Tile
import com.mygdx.game.{Assets, Constants}

case class CreatureRenderer(creatureId: String) extends Renderable {

  private var sprite: Sprite = _

  private var facingTextures: Array[TextureRegion] = _
  private var runningAnimations: Array[Animation[TextureRegion]] = _

  private var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    sprite = new Sprite()

    facingTextures = new Array[TextureRegion](4)

    runningAnimations = new Array[Animation[TextureRegion]](4)

    val creature = gameState.creatures(creatureId)

    textureRegion = Assets.atlas.get.findRegion(creature.params.textureName)

    for (i <- 0 until 4)
      facingTextures(i) = new TextureRegion(
        textureRegion,
        creature.params.neutralStanceFrame * Constants.TileTextureWidth,
        i * Constants.TileTextureHeight,
        Constants.TileTextureWidth,
        Constants.TileTextureHeight
      )

    for (i <- 0 until 4) {
      val frames =
        for {
          j <- (0 until creature.params.frameCount).toArray
        } yield new TextureRegion(
          textureRegion,
          j * Constants.TileTextureWidth,
          i * Constants.TileTextureHeight,
          Constants.TileTextureWidth,
          Constants.TileTextureHeight
        )
      runningAnimations(i) =
        new Animation[TextureRegion](creature.params.frameDuration, frames: _*)
    }
  }

  override def pos(gameState: GameState): (Float, Float) = {
    val creature = gameState.creatures(creatureId)

    (creature.params.x, creature.params.y)
  }

  override def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    val frame = if (creature.moving) {
      runningAnimations(creature.params.dirMap(creature.facingDirection))
        .getKeyFrame(creature.params.animationTimer.time, true)
    } else {
      facingTextures(creature.params.dirMap(creature.facingDirection))
    }

    val (x, y) =
      Tile.translateIsoToScreen(creature.params.x - 0.5f + 0.85f, creature.params.y - 0.5f - 0.85f)

    batch.draw(frame, x, y)
  }

}
