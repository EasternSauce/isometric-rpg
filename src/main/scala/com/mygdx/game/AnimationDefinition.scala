package com.mygdx.game

case class AnimationDefinition(
    stanceFrames: FramesDefinition,
    walkFrames: FramesDefinition,
    attackFrames: FramesDefinition,
    deathFrames: FramesDefinition,
    spellcastFrames: Option[FramesDefinition] = None,
    bowFrames: Option[FramesDefinition] = None
) {}
