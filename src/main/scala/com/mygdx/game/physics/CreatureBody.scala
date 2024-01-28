package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}

case class CreatureBody(creatureId: EntityId[Creature]) extends PhysicsBody {
  var body: Body = _

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

  def update(gameState: GameState): Unit = {
    val creature = gameState.creatures(creatureId)

    body.setLinearVelocity(creature.params.velocityX, creature.params.velocityY)
  }

  def getPos: (Float, Float) = {
    (body.getPosition.x, body.getPosition.y)
  }
}
