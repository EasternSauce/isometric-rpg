package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.mygdx.game.gamestate.{Creature, EntityId}

case class CreatureBody(creatureId: EntityId[Creature]) extends PhysicsBody {
  var body: Body = _

  private var velocityX: Float = 0
  private var velocityY: Float = 0

  def init(world: World, x: Float, y: Float): Unit = {
    this.body = {
      import com.badlogic.gdx.physics.box2d._

      val bodyDef = new BodyDef()
      bodyDef.`type` = BodyType.DynamicBody
      bodyDef.position.set(x, y)

      val body = world.createBody(bodyDef)

      val fixtureDef = new FixtureDef()
      val shape = new CircleShape()
      shape.setRadius(0.2f)
      fixtureDef.shape = shape

      body.createFixture(fixtureDef)

      body.setLinearDamping(10f)

      val massData = new MassData
      massData.mass = 1000f
      body.setMassData(massData)

      body
    }
  }

  def move(vx: Float, vy: Float): Unit = {
    velocityX = vx
    velocityY = vy
  }

  def update(): Unit = {
    body.setLinearVelocity(velocityX, velocityY)
    velocityX = 0
    velocityY = 0
  }

  def getPos: (Float, Float) = {
    (body.getPosition.x, body.getPosition.y)
  }
}
