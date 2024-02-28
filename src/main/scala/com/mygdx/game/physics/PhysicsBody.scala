package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.Body
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.Vector2

trait PhysicsBody {
  protected var body: Body = _

  def init(world: World, pos: Vector2, gameState: GameState): Unit

  def update(gameState: GameState): Unit

  def makeSensor(): Unit = {
    body.getFixtureList.get(0).setSensor(true)
  }

  def makeNonSensor(): Unit = {
    body.getFixtureList.get(0).setSensor(false)
  }

  def setPos(pos: Vector2): Unit = {
    body.setTransform(
      pos.x,
      pos.y,
      body.getAngle
    )
  }

  def pos: Vector2 = {
    Vector2(body.getPosition.x, body.getPosition.y)
  }
}
