package com.mygdx.game.gamestate

import com.mygdx.game.util.{SimpleTimer, Vector2}
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureParams(
    id: EntityId[Creature],
    pos: Vector2,
    velocity: Vector2,
    destination: Vector2,
    lastVelocity: Vector2,
    lastPos: Vector2,
    textureNames: Map[CreatureAnimationType, String],
    animationTimer: SimpleTimer,
    lastPosTimer: SimpleTimer,
    attackTimer: SimpleTimer,
    player: Boolean,
    baseVelocity: Float
) {}
