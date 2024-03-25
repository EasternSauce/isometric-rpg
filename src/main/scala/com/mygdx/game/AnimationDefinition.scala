package com.mygdx.game

case class AnimationDefinition(
    frameWidth: Int,
    frameHeight: Int,
    stanceFrames: FramesDefinition,
    walkFrames: FramesDefinition,
    attackFrames: FramesDefinition,
    deathFrames: FramesDefinition,
    spellcastFrames: Option[FramesDefinition] = None,
    bowFrames: Option[FramesDefinition] = None
) {}
