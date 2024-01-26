package com.mygdx.game.gamestate

import com.mygdx.game.util.SimpleTimer

case class CreatureParams(
    id: String,
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
    textureName: String,
    neutralStanceFrame: Int,
    animationTimer: SimpleTimer,
    lastPosTimer: SimpleTimer
) {}
