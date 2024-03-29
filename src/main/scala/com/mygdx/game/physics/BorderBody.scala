package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{BodyDef, FixtureDef, PolygonShape}
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.util.Vector2

case class BorderBody(borderId: String) extends PhysicsBody {
  def init(world: World, pos: Vector2, gameState: GameState): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.`type` = BodyType.StaticBody
    bodyDef.position.set(pos.x + 0.5f, pos.y + 0.5f)

    val body = world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef = new FixtureDef()
    val shape = new PolygonShape()
    shape.setAsBox(0.5f, 0.5f)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)

    this.b2Body = body
  }

  override def update(gameState: GameState): Unit = {}

  override def onRemove(): Unit = {}
}
