package com.mygdx.game.physics

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event._
import com.mygdx.game.gamestate.event.physics.{MakeBodyNonSensorEvent, MakeBodySensorEvent, PhysicsEvent, TeleportEvent}
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.Vector2

case class Physics() {
  private var world: World = _
  private var creatureBodies: Map[EntityId[Creature], CreatureBody] = _
  private var abilityBodies: Map[EntityId[Ability], AbilityBody] = _
  private var staticBodies: List[PhysicsBody] = _
  private var eventQueue: List[PhysicsEvent] = _
  private var collisionQueue: List[GameStateEvent] = _

  def init(
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {
    world = World()
    world.init(PhysicsContactListener(this))

    creatureBodies = Map()
    abilityBodies = Map()

    val cells = levelMap.getLayerCells(0) ++ levelMap.getLayerCells(1)

    val borders =
      ((1 until levelMap.getMapWidth - 1).zip(LazyList.continually(0)) ++
        LazyList.continually(0).zip(1 until levelMap.getMapHeight - 1) ++
        LazyList
          .continually(levelMap.getMapWidth - 1)
          .zip(1 until levelMap.getMapHeight - 1) ++
        (1 until levelMap.getMapWidth - 1).zip(
          LazyList.continually(levelMap.getMapHeight - 1)
        ))
        .map { case (x, y) => Vector2(x, y) }

    staticBodies =
      cells.filterNot(_.walkable).map(_.pos(gameState)).distinct.map { pos =>
        val terrainBody = TerrainBody("terrainBody_" + pos.x + "_" + pos.y)
        terrainBody.init(world, pos, gameState)
        terrainBody
      } ++ borders.map { pos =>
        val borderBody = BorderBody("borderBody_" + pos.x + "_" + pos.y)
        borderBody.init(world, pos, gameState)
        borderBody
      }

    eventQueue = List()
    collisionQueue = List()
  }

  def update(gameState: GameState): Unit = {
    world.update()

    handleEvents(eventQueue, gameState)

    correctBodyPositions(gameState)

    synchronizeWithGameState(gameState)

    creatureBodies.values.foreach(_.update(gameState))
    abilityBodies.values.foreach(_.update(gameState))
  }

  private def synchronizeWithGameState(gameState: GameState): Unit = {
    val creatureBodiesToCreate =
      gameState.activeCreatureIds -- creatureBodies.keys.toSet
    val creatureBodiesToDestroy =
      creatureBodies.keys.toSet -- gameState.activeCreatureIds

    creatureBodiesToCreate.foreach(createCreatureBody(_, gameState))
    creatureBodiesToDestroy.foreach(destroyCreatureBody(_, gameState))

    val abilityBodiesToCreate =
      gameState.abilities.keys.toSet -- abilityBodies.keys.toSet
    val abilityBodiesToDestroy =
      abilityBodies.keys.toSet -- gameState.abilities.keys.toSet

    abilityBodiesToCreate.foreach(createAbilityBody(_, gameState))
    abilityBodiesToDestroy.foreach(destroyAbilityBody(_, gameState))

    gameState.activeCreatureIds.foreach(creatureId => {
      val creature = gameState.creatures(creatureId)

      if (creature.alive) {
        if (creatureBodies(creature.id).sensor) {
          creatureBodies(creature.id).setNonSensor()
        }
      } else {
        if (!creatureBodies(creature.id).sensor) {
          creatureBodies(creature.id).setSensor()
        }
      }
    })
  }

  private def correctBodyPositions(gameState: GameState): Unit = {
    gameState.creatures.values.foreach(creature =>
      if (
        creatureBodies.contains(creature.id) && creatureBodies(creature.id).pos
          .distance(creature.pos) > Constants.PhysicalBodyCorrectionDistance
      ) {
        creatureBodies(creature.id).setPos(creature.pos)
      }
    )
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
          creatureBodies(creature.id).setPos(pos)
        }
      case MakeBodySensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          val creature = gameState.creatures(creatureId)
          creatureBodies(creature.id).setSensor()
        }
      case MakeBodyNonSensorEvent(creatureId) =>
        if (gameState.creatures.contains(creatureId)) {
          val creature = gameState.creatures(creatureId)
          creatureBodies(creature.id).setNonSensor()
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

  def creatureBodyPositions: Map[EntityId[Creature], Vector2] = {
    creatureBodies.values
      .map(creatureBody => {
        val pos = creatureBody.pos
        (creatureBody.creatureId, pos)
      })
      .toMap
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

  private def createCreatureBody(
      creatureId: EntityId[Creature],
      gameState: GameState
  ): Unit = {
    val creature = gameState.creatures(creatureId)

    val creatureBody = CreatureBody(creatureId)

    creatureBody.init(world, creature.pos, gameState)

    creatureBodies = creatureBodies.updated(creatureId, creatureBody)
  }

  private def destroyCreatureBody(
      creatureId: EntityId[Creature],
      gameState: GameState
  ): Unit = {
    if (creatureBodies.contains(creatureId)) {
      creatureBodies(creatureId).onRemove()
      creatureBodies = creatureBodies.removed(creatureId)
    }
  }

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

}
