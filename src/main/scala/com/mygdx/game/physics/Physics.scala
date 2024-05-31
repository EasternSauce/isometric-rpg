package com.mygdx.game.physics

import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event._
import com.mygdx.game.gamestate.event.physics.{MakeBodyNonSensorEvent, MakeBodySensorEvent, PhysicsEvent, TeleportEvent}
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2

case class Physics() {
  private var _world: World = _
  private var creatureBodyPhysics: CreatureBodyPhysics = _
  private var abilityBodyPhysics: AbilityBodyPhysics = _
  private var staticBodyPhysics: StaticBodyPhysics = _
  private var eventQueue: List[PhysicsEvent] = _
  private var collisionQueue: List[GameStateEvent] = _

  def init(
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    _world = World()
    _world.init(PhysicsContactListener(this))

    creatureBodyPhysics = CreatureBodyPhysics()
    creatureBodyPhysics.init(_world)

    abilityBodyPhysics = AbilityBodyPhysics()
    abilityBodyPhysics.init(_world)

    staticBodyPhysics = StaticBodyPhysics()
    staticBodyPhysics.init(tiledMap, world, gameState)

    eventQueue = List()
    collisionQueue = List()
  }

  def update(gameState: GameState): Unit = {
    _world.update()

    handleEvents(eventQueue, gameState)

    correctBodyPositions(gameState)

    synchronizeWithGameState(gameState)

    updateBodies(gameState)
  }

  private def updateBodies(gameState: GameState): Unit = {
    creatureBodyPhysics.update(gameState)
    abilityBodyPhysics.update(gameState)
  }

  private def synchronizeWithGameState(gameState: GameState): Unit = {
    creatureBodyPhysics.synchronizeWithGameState(gameState)
    abilityBodyPhysics.symchronizeWithGameState(gameState)
  }

  private def correctBodyPositions(gameState: GameState): Unit = {
    creatureBodyPhysics.correctBodyPositions(gameState)
    abilityBodyPhysics.correctBodyPositions(gameState)
  }

  private def handleEvents(
      eventsToBeProcessed: List[PhysicsEvent],
      gameState: GameState
  ): Unit = {
    eventsToBeProcessed.foreach {
      case TeleportEvent(creatureId, pos) =>
        if (gameState.creatures.contains(creatureId)) {
          creatureBodyPhysics.setBodyPos(creatureId, pos)
        }
      case MakeBodySensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          creatureBodyPhysics.setSensor(creatureId)
        }
      case MakeBodyNonSensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          creatureBodyPhysics.setNonSensor(creatureId)
        }
      case _ =>
    }

    eventQueue = eventQueue.filter(!eventsToBeProcessed.contains(_))
  }

  def pollCollisionEvents(): List[GameStateEvent] = {
    val collisionEvents = collisionQueue

    collisionQueue = List()

    collisionEvents
  }

  def scheduleEvents(events: List[PhysicsEvent]): Unit = {
    eventQueue = eventQueue.appendedAll(events)
  }

  def scheduleCollisions(collisions: List[GameStateEvent]): Unit = {
    collisionQueue = collisionQueue.appendedAll(collisions)
  }

  def world: World = _world

  def creatureBodyPositions: Map[EntityId[Creature], Vector2] =
    creatureBodyPhysics.creatureBodyPositions

  def abilityBodyPositions: Map[EntityId[Ability], Vector2] =
    abilityBodyPhysics.abilityBodyPositions
}
