package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.Body
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.Vector2

trait PhysicsBody {
  protected var b2Body: Body = _
  protected var areaWorld: AreaWorld = _
  protected var _sensor: Boolean = false

  def init(areaWorld: AreaWorld, pos: Vector2, gameState: GameState): Unit

  def update(gameState: GameState): Unit

  def sensor: Boolean = _sensor

  def setSensor(): Unit = {
    b2Body.getFixtureList.get(0).setSensor(true)
    _sensor = true
  }

  def setNonSensor(): Unit = {
    b2Body.getFixtureList.get(0).setSensor(false)
    _sensor = false
  }

  def setPos(pos: Vector2): Unit = {
    b2Body.setTransform(
      pos.x,
      pos.y,
      b2Body.getAngle
    )
  }

  def pos: Vector2 = {
    Vector2(b2Body.getPosition.x, b2Body.getPosition.y)
  }

  def onRemove(): Unit
}
