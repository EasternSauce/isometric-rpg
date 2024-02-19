package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityBody(abilityId: EntityId[Ability]) extends PhysicsBody {
  override def init(world: World, pos: Vector2): Unit = {
    this.body = {
      import com.badlogic.gdx.physics.box2d._

      val bodyDef = new BodyDef()
      bodyDef.`type` = BodyType.DynamicBody
      bodyDef.position.set(pos.x, pos.y)

      val body = world.createBody(bodyDef)

      val fixtureDef = new FixtureDef()
      val shape = new CircleShape()
      shape.setRadius(0.2f)
      fixtureDef.shape = shape
      fixtureDef.isSensor = true

      body.createFixture(fixtureDef)

      body
    }
  }

  override def update(gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    body.setLinearVelocity(ability.params.velocity.x, ability.params.velocity.y)
  }
}
