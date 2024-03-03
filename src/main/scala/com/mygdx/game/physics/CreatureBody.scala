package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class CreatureBody(creatureId: EntityId[Creature]) extends PhysicsBody {
  var world: World = _

  def init(world: World, pos: Vector2, gameState: GameState): Unit = {
    this.b2Body = {
      val creature = gameState.creatures(creatureId)

      import com.badlogic.gdx.physics.box2d._

      val bodyDef = new BodyDef()
      bodyDef.`type` = BodyType.DynamicBody
      bodyDef.position.set(pos.x, pos.y)

      val body = world.createBody(bodyDef)
      body.setUserData(this)

      val fixtureDef = new FixtureDef()
      val shape = new CircleShape()
      shape.setRadius(creature.params.bodyRadius)
      fixtureDef.shape = shape

      body.createFixture(fixtureDef)

      body.setLinearDamping(10f)

      val massData = new MassData
      massData.mass = 1000f
      body.setMassData(massData)

      body
    }

    this.world = world
  }

  override def update(gameState: GameState): Unit = {
    if (gameState.creatures.contains(creatureId)) {
      val creature = gameState.creatures(creatureId)

      b2Body.setLinearVelocity(
        creature.params.velocity.x,
        creature.params.velocity.y
      )
    }
  }

  override def onRemove(): Unit = {
    world.b2World.destroyBody(b2Body)
  }
}
