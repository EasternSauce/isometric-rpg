package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, Box2DDebugRenderer, World => B2World}

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

  def update(): Unit = {
    b2World.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)
  }

  def createBody(bodyDef: BodyDef): Body = {
    b2World.createBody(bodyDef)
  }

}
