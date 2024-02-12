package com.mygdx.game.physics

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event._
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap
import com.mygdx.game.util.Vector2

case class Physics() {
  private var world: World = _
  private var creatureBodies: Map[EntityId[Creature], CreatureBody] = _
  private var staticBodies: List[PhysicsBody] = _
  private var clientInformation: ClientInformation = _
  private var eventQueue: List[Event] = _

  def init(
      clientInformation: ClientInformation,
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {
    world = World()
    world.init()

    val player = gameState.creatures(clientInformation.clientCreatureId)
    val playerBody = CreatureBody(clientInformation.clientCreatureId)
    playerBody.init(world, player.pos)

    creatureBodies = Map(clientInformation.clientCreatureId -> playerBody)

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
        terrainBody.init(world, pos)
        terrainBody
      } ++ borders.map { pos =>
        val borderBody = BorderBody("borderBody_" + pos.x + "_" + pos.y)
        borderBody.init(world, pos)
        borderBody
      }

    eventQueue = List()
  }

  def update(gameState: GameState): Unit = {
    world.update()

    val bodiesToCreate =
      gameState.creatures.keys.toSet -- creatureBodies.keys.toSet

    bodiesToCreate.foreach { creatureId =>
      val creature = gameState.creatures(creatureId)

      val creatureBody = CreatureBody(creatureId)

      creatureBody.init(world, creature.pos)

      creatureBodies = creatureBodies.updated(creatureId, creatureBody)
    }

    val eventsToBeProcessed = eventQueue.filter(_.isInstanceOf[PhysicsEvent])

    handleEvents(eventsToBeProcessed, gameState)

    creatureBodies.values.foreach(_.update(gameState))
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
    }

    eventQueue = eventQueue.filter(!eventsToBeProcessed.contains(_))
  }

  def scheduleEvent(event: Event): Unit = {
    eventQueue = eventQueue.appended(event)
  }

  def getCreaturePositions: Map[EntityId[Creature], Vector2] = {
    creatureBodies.values
      .map(creatureBody => {
        val pos = creatureBody.pos
        (creatureBody.creatureId, pos)
      })
      .toMap
  }

  def getWorld: World = world

}
