package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.util.{SimpleTimer, Vector2}
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureParams(
    id: EntityId[Creature],
    pos: Vector2,
    velocity: Vector2,
    destination: Vector2,
    facingVector: Vector2,
    lastPos: Vector2,
    textureNames: Map[CreatureAnimationType, String],
    animationTimer: SimpleTimer,
    lastPosTimer: SimpleTimer,
    attackAnimationTimer: SimpleTimer,
    player: Boolean,
    baseVelocity: Float,
    life: Float,
    maxLife: Float,
    attackedCreatureId: Option[EntityId[Creature]],
    damage: Float,
    deathRegistered: Boolean,
    deathAnimationTimer: SimpleTimer
) {}
