package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.creature.PrimaryWeaponType.PrimaryWeaponType
import com.mygdx.game.gamestate.creature.SecondaryWeaponType.SecondaryWeaponType
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.CreatureAnimationType
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType
import com.mygdx.game.{AnimationDefinition, Constants}

object CreatureFactory {
  def male1(
      creatureId: EntityId[Creature],
      currentAreaId: AreaId,
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      currentAreaId = currentAreaId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 100f,
      damage = 20f,
      attackRange = 2f,
      texturePaths = Map(
        CreatureAnimationType.Body -> "isometric_hero/clothes",
        CreatureAnimationType.Head -> "isometric_hero/male_head1",
        CreatureAnimationType.Weapon -> "isometric_hero/shortbow",
        CreatureAnimationType.Shield -> "isometric_hero/shield"
      ),
      size = 128,
      spriteVerticalShift = 10f,
      bodyRadius = 0.3f,
      animationDefinition = Constants.HumanAnimationDefinition,
      primaryWeaponType = PrimaryWeaponType.Bow,
      secondaryWeaponType = SecondaryWeaponType.None,
      renderBodyOnly = false
    )
  }

  def rat(
      creatureId: EntityId[Creature],
      currentAreaId: AreaId,
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      currentAreaId = currentAreaId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 40f,
      damage = 10f,
      attackRange = 1.6f,
      texturePaths = Map(
        CreatureAnimationType.Body -> "rat/rat"
      ),
      size = 192,
      spriteVerticalShift = 35f,
      bodyRadius = 0.6f,
      animationDefinition = Constants.RatAnimationDefinition,
      primaryWeaponType = PrimaryWeaponType.None,
      secondaryWeaponType = SecondaryWeaponType.None,
      renderBodyOnly = true
    )
  }

  def zombie( // TODO: add random chances for alternate attack/death animation
      creatureId: EntityId[Creature],
      currentAreaId: AreaId,
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      currentAreaId = currentAreaId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 65f,
      damage = 20f,
      attackRange = 1.6f,
      texturePaths = Map(
        CreatureAnimationType.Body -> "zombie/zombie"
      ),
      size = 128,
      spriteVerticalShift = 10f,
      bodyRadius = 0.3f,
      animationDefinition = Constants.ZombieAnimationDefinition,
      primaryWeaponType = PrimaryWeaponType.None,
      secondaryWeaponType = SecondaryWeaponType.None,
      renderBodyOnly = true
    )
  }

  def wyvern( // TODO: add random chances for alternate attack/death animation
      creatureId: EntityId[Creature],
      currentAreaId: AreaId,
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      currentAreaId = currentAreaId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 200f,
      damage = 40f,
      attackRange = 2.2f,
      texturePaths = Map(
        CreatureAnimationType.Body -> "wyvern/wyvern"
      ),
      size = 256,
      spriteVerticalShift = 10f,
      bodyRadius = 0.8f,
      animationDefinition = Constants.WyvernAnimationDefinition,
      primaryWeaponType = PrimaryWeaponType.None,
      secondaryWeaponType = SecondaryWeaponType.None,
      renderBodyOnly = true
    )
  }

  private def produce(
      creatureId: EntityId[Creature],
      currentAreaId: AreaId,
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float,
      maxLife: Float,
      damage: Float,
      attackRange: Float,
      texturePaths: Map[CreatureAnimationType, String],
      size: Int,
      spriteVerticalShift: Float,
      bodyRadius: Float,
      animationDefinition: AnimationDefinition,
      primaryWeaponType: PrimaryWeaponType,
      secondaryWeaponType: SecondaryWeaponType,
      renderBodyOnly: Boolean
  ): Creature = {
    Creature(
      CreatureParams(
        id = creatureId,
        currentAreaId = currentAreaId,
        pos = pos,
        destination = pos,
        lastPos = pos,
        texturePaths = texturePaths,
        size = size,
        spriteVerticalShift = spriteVerticalShift,
        bodyRadius = bodyRadius,
        player = player,
        baseSpeed = baseSpeed,
        life = maxLife,
        maxLife = maxLife,
        damage = damage,
        animationDefinition = animationDefinition,
        attackRange = attackRange,
        primaryWeaponType = primaryWeaponType,
        secondaryWeaponType = secondaryWeaponType,
        renderBodyOnly = renderBodyOnly
      )
    )
  }
}
