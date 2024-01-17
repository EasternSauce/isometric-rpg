package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{BodyDef, Box2DDebugRenderer, FixtureDef, PolygonShape, World => B2World}

case class World() {
  var b2World: B2World = _
  var debugRenderer: Box2DDebugRenderer = _

  def init(): Unit = {
    b2World = new B2World(new Vector2(0, 0), true)
    debugRenderer = new Box2DDebugRenderer()
  }

  def renderDebug(b2DebugViewport: Viewport): Unit = {
    b2DebugViewport.renderB2Debug(debugRenderer, b2World)
  }

  def createBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef
    bodyDef.`type` = BodyType.DynamicBody
    bodyDef.position.set(x, y)

    val body = b2World.createBody(bodyDef)

    val fixtureDef = new FixtureDef
    val shape = new PolygonShape()
    shape.setAsBox(1f, 1f)
    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
  }

  def update(): Unit = {
    b2World.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)
  }
}
