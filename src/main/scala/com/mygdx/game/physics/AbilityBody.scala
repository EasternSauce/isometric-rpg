package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityBody(abilityId: EntityId[Ability]) extends PhysicsBody {
  var world: World = _

  override def init(world: World, pos: Vector2, gameState: GameState): Unit = {
    this.b2Body = {
      import com.badlogic.gdx.physics.box2d._

      val bodyDef = new BodyDef()
      bodyDef.`type` = BodyType.DynamicBody
      bodyDef.position.set(pos.x, pos.y)

      val body = world.createBody(bodyDef)
      body.setUserData(this)

      val fixtureDef = new FixtureDef()
      val shape = new CircleShape()
      shape.setRadius(0.3f)
      fixtureDef.shape = shape
      fixtureDef.isSensor = true

      body.createFixture(fixtureDef)

      body
    }

    this.world = world
  }

  override def update(gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    b2Body.setLinearVelocity(ability.velocity.x, ability.velocity.y)
  }

  override def onRemove(): Unit = {
    world.b2World.destroyBody(b2Body)
  }
}
