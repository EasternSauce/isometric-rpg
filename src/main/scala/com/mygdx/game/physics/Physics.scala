package com.mygdx.game.physics

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event._
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.Vector2

case class Physics() {
  private var world: World = _
  private var creatureBodies: Map[EntityId[Creature], CreatureBody] = _
  private var abilityBodies: Map[EntityId[Ability], AbilityBody] = _
  private var staticBodies: List[PhysicsBody] = _
  private var clientInformation: ClientInformation = _
  private var eventQueue: List[Event] = _
  private var collisionQueue: List[CollisionEvent] = _

  def init(
      clientInformation: ClientInformation,
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {
    world = World()
    world.init(PhysicsContactListener(this))

    val player = gameState.creatures(clientInformation.clientCreatureId)
    val playerBody = CreatureBody(clientInformation.clientCreatureId)
    playerBody.init(world, player.pos, gameState)

    creatureBodies = Map(clientInformation.clientCreatureId -> playerBody)
    abilityBodies = Map()

    this.clientInformation = clientInformation

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

    val creatureBodiesToCreate =
      gameState.creatures.keys.toSet -- creatureBodies.keys.toSet

    creatureBodiesToCreate.foreach { creatureId =>
      val creature = gameState.creatures(creatureId)

      val creatureBody = CreatureBody(creatureId)

      creatureBody.init(world, creature.pos, gameState)

      creatureBodies = creatureBodies.updated(creatureId, creatureBody)
    }

    val abilityBodiesToCreate =
      gameState.abilities.keys.toSet -- abilityBodies.keys.toSet

    abilityBodiesToCreate.foreach { abilityId =>
      val creature = gameState.abilities(abilityId)

      val abilityBody = AbilityBody(abilityId)

      abilityBody.init(world, creature.pos, gameState)

      abilityBodies = abilityBodies.updated(abilityId, abilityBody)
    }

    handleEvents(eventQueue, gameState)

    creatureBodies.values.foreach(_.update(gameState))
    abilityBodies.values.foreach(_.update(gameState))
  }

  private def handleEvents(
      eventsToBeProcessed: List[Event],
      gameState: GameState
  ): Unit = {
    eventsToBeProcessed.foreach {
      case TeleportEvent(creatureId, pos) =>
        val creature = gameState.creatures(creatureId)
        creatureBodies(creature.id).setPos(pos)
      case MakeBodySensorEvent(creatureId) =>
        val creature = gameState.creatures(creatureId)
        creatureBodies(creature.id).makeSensor()
      case MakeBodyNonSensorEvent(creatureId) =>
        val creature = gameState.creatures(creatureId)
        creatureBodies(creature.id).makeNonSensor()
      case _ =>
    }

    eventQueue = eventQueue.filter(!eventsToBeProcessed.contains(_))
  }

  def pollCollisionEvents(): List[CollisionEvent] = {
    val collisionEvents = collisionQueue

    collisionQueue = List()

    collisionEvents
  }

  def scheduleEvents(events: List[Event]): Unit = {
    eventQueue = eventQueue.appendedAll(events)
  }

  def scheduleCollisions(collisions: List[CollisionEvent]): Unit = {
    collisionQueue = collisionQueue.appendedAll(collisions)
  }

  def getCreaturePositions: Map[EntityId[Creature], Vector2] = {
    creatureBodies.values
      .map(creatureBody => {
        val pos = creatureBody.pos
        (creatureBody.creatureId, pos)
      })
      .toMap
  }

  def getAbilityPositions: Map[EntityId[Ability], Vector2] = {
    abilityBodies.values
      .map(abilityBody => {
        val pos = abilityBody.pos
        (abilityBody.abilityId, pos)
      })
      .toMap
  }

  def getWorld: World = world

}
