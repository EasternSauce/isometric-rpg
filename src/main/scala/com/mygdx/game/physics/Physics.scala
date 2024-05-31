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
  private var world: World = _
  private var creatureBodyPhysics: CreatureBodyPhysics = _
  private var abilityBodies: Map[EntityId[Ability], AbilityBody] = _
  private var staticBodies: List[PhysicsBody] = _
  private var eventQueue: List[PhysicsEvent] = _
  private var collisionQueue: List[GameStateEvent] = _

  def init(
      tiledMap: TiledMap,
      gameState: GameState
  ): Unit = {
    world = World()
    world.init(PhysicsContactListener(this))

    creatureBodyPhysics = CreatureBodyPhysics()
    creatureBodyPhysics.init(world)

    abilityBodies = Map()

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
        terrainBody.init(world, pos, gameState)
        terrainBody
    } ++
      objectCollisions.map(_.pos(gameState)).distinct.map { pos =>
        val objectBody = ObjectBody("objectBody_" + pos.x + "_" + pos.y)
        objectBody.init(world, pos, gameState)
        objectBody
      }
  }

  def update(gameState: GameState): Unit = {
    world.update()

    handleEvents(eventQueue, gameState)

    correctBodyPositions(gameState)

    synchronizeWithGameState(gameState)

    creatureBodyPhysics.update(gameState)
    abilityBodies.values.foreach(_.update(gameState))
  }

  private def synchronizeWithGameState(gameState: GameState): Unit = {
    creatureBodyPhysics.synchronizeWithGameState(gameState)

    val abilityBodiesToCreate =
      gameState.abilities.keys.toSet -- abilityBodies.keys.toSet
    val abilityBodiesToDestroy =
      abilityBodies.keys.toSet -- gameState.abilities.keys.toSet

    abilityBodiesToCreate.foreach(createAbilityBody(_, gameState))
    abilityBodiesToDestroy.foreach(destroyAbilityBody(_, gameState))
  }

  private def correctBodyPositions(gameState: GameState): Unit = {
    creatureBodyPhysics.correctBodyPositions(gameState)

    gameState.abilities.values.foreach(ability =>
      if (
        abilityBodies.contains(ability.id) && abilityBodies(ability.id).pos
          .distance(ability.pos) > Constants.PhysicalBodyCorrectionDistance
      ) {
        abilityBodies(ability.id).setPos(ability.pos)
      }
    )
  }

  private def handleEvents(
      eventsToBeProcessed: List[PhysicsEvent],
      gameState: GameState
  ): Unit = {
    eventsToBeProcessed.foreach {
      case TeleportEvent(creatureId, pos) =>
        if (gameState.creatures.contains(creatureId)) {
          val creature = gameState.creatures(creatureId)
          creatureBodyPhysics.setBodyPos(creatureId, pos)
        }
      case MakeBodySensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          val creature = gameState.creatures(creatureId)
          creatureBodyPhysics.setSensor(creatureId)
        }
      case MakeBodyNonSensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          val creature = gameState.creatures(creatureId)
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

  def abilityBodyPositions: Map[EntityId[Ability], Vector2] = {
    abilityBodies.values
      .map(abilityBody => {
        val pos = abilityBody.pos
        (abilityBody.abilityId, pos)
      })
      .toMap
  }

  def getWorld: World = world

  private def createAbilityBody(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    val creature = gameState.abilities(abilityId)

    val abilityBody = AbilityBody(abilityId)

    abilityBody.init(world, creature.pos, gameState)

    abilityBodies = abilityBodies.updated(abilityId, abilityBody)
  }

  private def destroyAbilityBody(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    if (abilityBodies.contains(abilityId)) {
      abilityBodies(abilityId).onRemove()
      abilityBodies = abilityBodies.removed(abilityId)
    }
  }

  def creatureBodyPositions: Map[EntityId[Creature], Vector2] =
    creatureBodyPhysics.creatureBodyPositions
}
