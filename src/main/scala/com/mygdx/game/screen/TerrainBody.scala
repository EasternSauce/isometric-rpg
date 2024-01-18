package com.mygdx.game.screen

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape}

case class TerrainBody(terrainId: String, x: Float, y: Float) {
  var body: Body = _

  def init(world: World, x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.`type` = BodyType.StaticBody
    bodyDef.position.set(x + 0.5f, y + 0.5f)

    val body = world.createBody(bodyDef)

    val fixtureDef = new FixtureDef()
    val shape = new PolygonShape()
    shape.setAsBox(0.5f, 0.5f)
    fixtureDef.shape = shape

    body.createFixture(fixtureDef)

    this.body = body
  }

}
