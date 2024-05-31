package com.mygdx.game.physics

import com.mygdx.game.Constants
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
  private var staticBodies: List[PhysicsBody] = _
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

    staticBodies = createStaticBodies(tiledMap, gameState)

    eventQueue = List()
    collisionQueue = List()
  }

  private def createStaticBodies(
      tiledMap: TiledMap,
      gameState: GameState
  ): List[PhysicsBody] = {
    val bigObjects = tiledMap.getLayer("object")

    val terrainCollisions =
      tiledMap
        .getLayer("collision")
        .filter(_.cell.getTile.getId == Constants.waterGroundCollisionCellId) ++
        tiledMap
          .getLayer("manual_collision")
          .filter(_.cell.getTile.getId == Constants.waterGroundCollisionCellId)

    val objectCollisions =
      tiledMap
        .getLayer("collision")
        .filter(_.cell.getTile.getId == Constants.objectCollisionCellId) ++
        tiledMap
          .getLayer("manual_collision")
          .filter(_.cell.getTile.getId == Constants.objectCollisionCellId)

    (bigObjects ++ terrainCollisions).map(_.pos(gameState)).distinct.map {
      pos =>
        val terrainBody = TerrainBody("terrainBody_" + pos.x + "_" + pos.y)
        terrainBody.init(_world, pos, gameState)
        terrainBody
    } ++
      objectCollisions.map(_.pos(gameState)).distinct.map { pos =>
        val objectBody = ObjectBody("objectBody_" + pos.x + "_" + pos.y)
        objectBody.init(_world, pos, gameState)
        objectBody
      }
  }

  def update(gameState: GameState): Unit = {
    _world.update()

    handleEvents(eventQueue, gameState)

    correctBodyPositions(gameState)

    synchronizeWithGameState(gameState)

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
