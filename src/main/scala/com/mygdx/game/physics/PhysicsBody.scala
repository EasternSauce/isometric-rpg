package com.mygdx.game.physics

trait PhysicsBody {
  def init(world: World, x: Float, y: Float): Unit
}
