package com.mygdx.game.gamestate

import com.mygdx.game.util.SimpleTimer
import com.mygdx.game.view.CreatureAnimationType.CreatureAnimationType

case class CreatureParams(
    id: EntityId[Creature],
    x: Float,
    y: Float,
    velocityX: Float,
    velocityY: Float,
    destinationX: Float,
    destinationY: Float,
    lastVelocityX: Float,
    lastVelocityY: Float,
    lastPosX: Float,
    lastPosY: Float,
    textureNames: Map[CreatureAnimationType, String],
    animationTimer: SimpleTimer,
    lastPosTimer: SimpleTimer,
    attackTimer: SimpleTimer,
    player: Boolean
) {}
