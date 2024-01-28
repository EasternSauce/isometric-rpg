package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape}
import com.mygdx.game.util.Vector2

case class BorderBody(borderId: String) extends PhysicsBody {
  var body: Body = _

  def init(world: World, pos: Vector2): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.`type` = BodyType.StaticBody
    bodyDef.position.set(pos.x + 0.5f, pos.y + 0.5f)

    val body = world.createBody(bodyDef)

    val fixtureDef = new FixtureDef()
    val shape = new PolygonShape()
    shape.setAsBox(0.5f, 0.5f)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)

    this.body = body
  }

}
