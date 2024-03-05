package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.event.Event
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.gamestate.event.collision.CollisionEvent
import com.mygdx.game.gamestate.event.gamestate.GameStateEvent
import com.mygdx.game.gamestate.event.physics.PhysicsEvent

case class GameStateSideEffectsCollector(var events: List[Event]) {
  def gameStateEvents: List[GameStateEvent] = events
    .filter(_.isInstanceOf[GameStateEvent])
    .map(_.asInstanceOf[GameStateEvent])

  def broadcastEvents: List[BroadcastEvent] = events
    .filter(_.isInstanceOf[BroadcastEvent])
    .map(_.asInstanceOf[BroadcastEvent])

  def physicsEvents: List[PhysicsEvent] = events
    .filter(_.isInstanceOf[PhysicsEvent])
    .map(_.asInstanceOf[PhysicsEvent])

  def collisionEvents: List[CollisionEvent] = events
    .filter(_.isInstanceOf[CollisionEvent])
    .map(_.asInstanceOf[CollisionEvent])
}
