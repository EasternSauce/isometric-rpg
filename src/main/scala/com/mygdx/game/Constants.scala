package com.mygdx.game

object Constants {

  val WindowWidth = 1360
  val WindowHeight = 720

  val ViewpointWorldWidth = 1650f
  val ViewpointWorldHeight = 864f

  val TileSize = 64

  val EnableDebug = false

  val MapTextureScale = 2f

  val HumanAnimationDefinition: AnimationDefinition = AnimationDefinition(
    stanceFrames = FramesDefinition(start = 0, count = 4, frameDuration = 0.3f),
    walkFrames = FramesDefinition(start = 4, count = 8, frameDuration = 0.075f),
    attackFrames =
      FramesDefinition(start = 12, count = 4, frameDuration = 0.1f),
    deathFrames =
      FramesDefinition(start = 16, count = 8, frameDuration = 0.12f),
    spellcastFrames =
      Some(FramesDefinition(start = 24, count = 4, frameDuration = 0.3f)),
    bowFrames =
      Some(FramesDefinition(start = 28, count = 4, frameDuration = 0.1f))
  )

  val RatAnimationDefinition: AnimationDefinition = AnimationDefinition(
    stanceFrames = FramesDefinition(start = 0, count = 4, frameDuration = 0.2f),
    walkFrames = FramesDefinition(start = 4, count = 8, frameDuration = 0.08f),
    attackFrames =
      FramesDefinition(start = 12, count = 7, frameDuration = 0.16f),
    deathFrames =
      FramesDefinition(start = 17, count = 11, frameDuration = 0.09f)
  )

  val AttackCooldown = 0.3f

  val SpriteTextureWidth = 128
  val SpriteTextureHeight = 128

  val TileCenterX = 0
  val TileCenterY = 0

  val EnemyAggroDistance = 5f

  val RespawnTime = 5f

  val EnemyLoseAggroRange = 8f
  val EnemyLoseAggroTime = 3f

  val RespawnDelayTime = 0.5f

  val AttackedByCreatureLoseAggroTime = 8f

  val TimeBetweenGameStateBroadcasts = 1f

  var PhysicalBodyCorrectionDistance = 0.5f

  val LastPosSetInterval = 0.5f

  val LastPosMinimumDifference = 0.2f

  val MinimumDistanceBetweenDestinations = 0.001f
}
