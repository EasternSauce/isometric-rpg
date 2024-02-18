package com.mygdx.game.gamestate.creature

import com.mygdx.game.AnimationDefinition
import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.util.{SimpleTimer, Vector2}
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureParams(
    id: EntityId[Creature],
    pos: Vector2,
    velocity: Vector2 = Vector2(0, 0),
    destination: Vector2,
    facingVector: Vector2 = Vector2(0, 0),
    lastPos: Vector2,
    textureNames: Map[CreatureAnimationType, String],
    animationTimer: SimpleTimer = SimpleTimer(running = true),
    lastPosTimer: SimpleTimer = SimpleTimer(running = true),
    attackAnimationTimer: SimpleTimer = SimpleTimer(running = false),
    player: Boolean,
    baseSpeed: Float,
    life: Float,
    maxLife: Float,
    attackedCreatureId: Option[EntityId[Creature]] = None,
    damage: Float,
    deathAcknowledged: Boolean = false,
    deathAnimationTimer: SimpleTimer = SimpleTimer(running = false),
    animationDefinition: AnimationDefinition,
    attackRange: Float,
    respawnTimer: SimpleTimer = SimpleTimer(running = false),
    currentTargetId: Option[EntityId[Creature]] = None,
    loseAggroTimer: SimpleTimer = SimpleTimer(running = false),
    attackPending: Boolean = false
) {}
