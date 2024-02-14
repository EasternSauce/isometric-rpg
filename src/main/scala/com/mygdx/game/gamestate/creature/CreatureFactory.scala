package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate.EntityId
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
        CreatureAnimationType.Body -> "steel_armor",
        CreatureAnimationType.Head -> "male_head1",
        CreatureAnimationType.Weapon -> "greatstaff",
        CreatureAnimationType.Shield -> "shield"
      ),
      animationDefinition = Constants.HumanAnimationDefinition
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
      animationDefinition = Constants.RatAnimationDefinition
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
      animationDefinition: AnimationDefinition
  ): Creature = {
    Creature(
      CreatureParams(
        id = creatureId,
        pos = pos,
        destination = pos,
        lastPos = pos,
        textureNames = textureNames,
        player = player,
        baseSpeed = baseSpeed,
        life = maxLife,
        maxLife = maxLife,
        damage = damage,
        animationDefinition = animationDefinition,
        attackRange = attackRange
      ),
      creatureBehavior = if (player) PlayerBehavior() else EnemyBehavior()
    )
  }
}
