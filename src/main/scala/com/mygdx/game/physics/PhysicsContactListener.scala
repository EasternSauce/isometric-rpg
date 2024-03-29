package com.mygdx.game.physics

import com.badlogic.gdx.physics.box2d.{Contact, ContactImpulse, ContactListener, Manifold}
import com.mygdx.game.gamestate.event.collision.{AbilityHitsCreatureGameStateEvent, AbilityHitsTerrainGameStateEvent}

case class PhysicsContactListener(physics: Physics) extends ContactListener {
  override def beginContact(contact: Contact): Unit = {
    val objA = contact.getFixtureA.getBody.getUserData
    val objB = contact.getFixtureB.getBody.getUserData

    onContactStart(objA, objB)
    onContactStart(objB, objA)
  }

  def onContactStart(objA: Any, objB: Any): Unit = {
    (objA, objB) match {
      case (abilityBody: AbilityBody, creatureBody: CreatureBody) =>
        physics.scheduleCollisions(
          List(
            AbilityHitsCreatureGameStateEvent(
              abilityBody.abilityId,
              creatureBody.creatureId
            )
          )
        )
      case (abilityBody: AbilityBody, terrainBody: TerrainBody) =>
        physics.scheduleCollisions(
          List(
            AbilityHitsTerrainGameStateEvent(
              abilityBody.abilityId,
              terrainBody.terrainId
            )
          )
        )
      case (abilityBody: AbilityBody, borderBody: BorderBody) =>
        physics.scheduleCollisions(
          List(
            AbilityHitsTerrainGameStateEvent(
              abilityBody.abilityId,
              borderBody.borderId
            )
          )
        )
      case _ =>
    }
  }

  override def endContact(contact: Contact): Unit = {}

  override def preSolve(contact: Contact, oldManifold: Manifold): Unit = {}

  override def postSolve(contact: Contact, impulse: ContactImpulse): Unit = {}

  def onContactEnd(objA: Any, objB: Any): Unit = {
    (objA, objB) match {
      case (abilityBody: AbilityBody, creatureBody: CreatureBody) =>
      case (abilityBody: AbilityBody, terrainBody: TerrainBody)   =>
      case _                                                      =>
    }
  }
}
