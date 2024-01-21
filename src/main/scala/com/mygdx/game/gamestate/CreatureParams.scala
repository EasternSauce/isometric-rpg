package com.mygdx.game.gamestate

import com.mygdx.game.util.SimpleTimer
import com.mygdx.game.util.WorldDirection.WorldDirection

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
    frameCount: Int,
    frameDuration: Float,
    dirMap: Map[WorldDirection, Int],
    animationTimer: SimpleTimer,
    lastPosTimer: SimpleTimer
) {}
