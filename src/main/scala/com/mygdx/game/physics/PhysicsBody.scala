package com.mygdx.game.physics

import com.mygdx.game.util.Vector2

trait PhysicsBody {
  def init(world: World, pos: Vector2): Unit
}
