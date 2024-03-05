package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.creature.PrimaryWeaponType.PrimaryWeaponType
import com.mygdx.game.gamestate.creature.SecondaryWeaponType.SecondaryWeaponType
import com.mygdx.game.gamestate.creature.behavior.{EnemyBehavior, PlayerBehavior}
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.CreatureAnimationType
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType
import com.mygdx.game.{AnimationDefinition, Constants}

object CreatureFactory {
  def male1(
      creatureId: EntityId[Creature],
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 100f,
      damage = 20f,
      attackRange = 1f,
      textureNames = Map(
        CreatureAnimationType.Body -> "clothes",
        CreatureAnimationType.Head -> "male_head1",
        CreatureAnimationType.Weapon -> "shortbow",
        CreatureAnimationType.Shield -> "shield"
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
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    CreatureFactory.produce(
      creatureId = creatureId,
      pos = pos,
      player = player,
      baseSpeed = baseSpeed,
      maxLife = 40f,
      damage = 10f,
      attackRange = 0.8f,
      textureNames = Map(
        CreatureAnimationType.Body -> "rat"
      ),
      size = 192,
      spriteVerticalShift = 35f,
      bodyRadius = 0.3f,
      animationDefinition = Constants.RatAnimationDefinition,
      primaryWeaponType = PrimaryWeaponType.None,
      secondaryWeaponType = SecondaryWeaponType.None,
      renderBodyOnly = true
    )
  }

  private def produce(
      creatureId: EntityId[Creature],
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float,
      maxLife: Float,
      damage: Float,
      attackRange: Float,
      textureNames: Map[CreatureAnimationType, String],
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
        pos = pos,
        destination = pos,
        lastPos = pos,
        textureNames = textureNames,
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
      ),
      behavior = if (player) PlayerBehavior() else EnemyBehavior()
    )
  }
}
