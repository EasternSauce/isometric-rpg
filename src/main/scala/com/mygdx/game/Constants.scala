package com.mygdx.game

object Constants {

  val WindowWidth = 1360
  val WindowHeight = 720

  val ViewportWorldWidth = 1650f
  val ViewportWorldHeight = 864f

  val TileSize = 64

  val EnableDebug = false

  val MapTextureScale = 1f

  val HumanAnimationDefinition: AnimationDefinition = AnimationDefinition(
    frameWidth = 128,
    frameHeight = 128,
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
    frameWidth = 128,
    frameHeight = 128,
    stanceFrames = FramesDefinition(start = 0, count = 4, frameDuration = 0.4f),
    walkFrames = FramesDefinition(start = 4, count = 8, frameDuration = 0.04f),
    attackFrames =
      FramesDefinition(start = 12, count = 7, frameDuration = 0.08f),
    deathFrames =
      FramesDefinition(start = 17, count = 11, frameDuration = 0.045f)
  )

  val ZombieAnimationDefinition: AnimationDefinition = AnimationDefinition(
    frameWidth = 128,
    frameHeight = 128,
    stanceFrames = FramesDefinition(start = 0, count = 4, frameDuration = 0.4f),
    walkFrames = FramesDefinition(start = 4, count = 8, frameDuration = 0.08f),
    attackFrames =
      FramesDefinition(start = 12, count = 4, frameDuration = 0.1f),
    deathFrames = FramesDefinition(start = 22, count = 6, frameDuration = 0.1f)
  )

  val WyvernAnimationDefinition: AnimationDefinition = AnimationDefinition(
    frameWidth = 256,
    frameHeight = 256,
    stanceFrames = FramesDefinition(start = 0, count = 8, frameDuration = 0.1f),
    walkFrames = FramesDefinition(start = 8, count = 8, frameDuration = 0.08f),
    attackFrames =
      FramesDefinition(start = 16, count = 8, frameDuration = 0.1f),
    deathFrames = FramesDefinition(start = 48, count = 8, frameDuration = 0.1f)
  )

  val AttackCooldown = 0.3f

  val TileCenterX = 0
  val TileCenterY = 0

  val EnemyAggroDistance = 10f

  val RespawnTime = 5f

  val EnemyLoseAggroRange = 16f
  val EnemyLoseAggroTime = 3f

  val RespawnDelayTime = 0.5f

  val AttackedByCreatureLoseAggroTime = 8f

  val TimeBetweenGameStateBroadcasts = 0.5f

  var PhysicalBodyCorrectionDistance = 0.5f

  val LastPosSetInterval = 0.5f

  val LastPosMinimumDifference = 0.4f

  val OfflineMode = true

  val InventoryX: Int = 0
  val InventoryY: Int = 200
  val InventoryMargin: Int = 15
  val InventorySlotSize: Int = 45
  val InventoryWidth: Int = 10
  val InventoryHeight: Int = 5

  def inventorySlotPositionX(x: Int): Int =
    Constants.InventoryMargin + Constants.InventoryX + (Constants.InventorySlotSize + 5) * x
  def inventorySlotPositionY(y: Int): Int =
    Constants.InventoryMargin + Constants.InventoryY + (Constants.InventorySlotSize + 5) * (Constants.InventoryHeight - y - 1)

  def equipmentSlotPositionX(x: Int): Int =
    Constants.InventoryMargin + Constants.EquipmentX
  def equipmentSlotPositionY(y: Int): Int =
    Constants.InventoryMargin + Constants.EquipmentY + (Constants.InventorySlotSize + 5) * (Constants.EquipmentSlotCount - y - 1)

  val EquipmentX: Int = 700
  val EquipmentY: Int = 200
  val EquipmentSlotCount = 6

  val equipmentSlotNames: Map[Int, String] = Map(
    0 -> "weapon",
    1 -> "shield",
    2 -> "ring",
    3 -> "body armor",
    4 -> "gloves",
    5 -> "boots"
  )

  val hoverItemInfoX: Int = 30
  val hoverItemInfoY: Int = 50
  val hoverItemInfoWidth: Int = 470
  val hoverItemInfoHeight: Int = 150

  val bigObjectCollisionCellId = 1
  val waterGroundCollisionCellId = 2
  val smallObjectCollisionCellId = 4
  val wallCollisionCellId = 6
}
