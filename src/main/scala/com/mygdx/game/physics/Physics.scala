package com.mygdx.game.physics

import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event._
import com.mygdx.game.gamestate.event.physics.{MakeBodyNonSensorEvent, MakeBodySensorEvent, PhysicsEvent, TeleportEvent}
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2

case class Physics() {
  private var _areaWorlds: Map[AreaId, AreaWorld] = _
  private var creatureBodyPhysics: CreatureBodyPhysics = _
  private var abilityBodyPhysics: AbilityBodyPhysics = _
  private var staticBodyPhysics: StaticBodyPhysics = _
  private var eventQueue: List[PhysicsEvent] = _
  private var collisionQueue: List[GameStateEvent] = _

  def init(
      tiledMaps: Map[AreaId, TiledMap],
      gameState: GameState
  ): Unit = {
    _areaWorlds = tiledMaps.map { case (areaId: AreaId, _) =>
      (areaId, AreaWorld(areaId))
    }
    _areaWorlds.values.foreach(_.init(PhysicsContactListener(this)))

    creatureBodyPhysics = CreatureBodyPhysics()
    creatureBodyPhysics.init(_areaWorlds)

    abilityBodyPhysics = AbilityBodyPhysics()
    abilityBodyPhysics.init(_areaWorlds)

    staticBodyPhysics = StaticBodyPhysics()
    staticBodyPhysics.init(tiledMaps, _areaWorlds, gameState)

    eventQueue = List()
    collisionQueue = List()
  }

  def updateForArea(areaId: AreaId, gameState: GameState): Unit = {
    _areaWorlds(areaId).update()

    handleEvents(eventQueue, areaId, gameState)

    correctBodyPositions(areaId, gameState)

    synchronizeWithGameState(areaId, gameState)

    updateBodies(areaId, gameState)
  }

  private def updateBodies(areaId: AreaId, gameState: GameState): Unit = {
    creatureBodyPhysics.update(areaId, gameState)
    abilityBodyPhysics.update(areaId, gameState)
  }

  private def synchronizeWithGameState(
      areaId: AreaId,
      gameState: GameState
  ): Unit = {
    creatureBodyPhysics.synchronizeWithGameState(areaId, gameState)
    abilityBodyPhysics.synchronizeWithGameState(areaId, gameState)
  }

  private def correctBodyPositions(
      areaId: AreaId,
      gameState: GameState
  ): Unit = {
    creatureBodyPhysics.correctBodyPositions(areaId, gameState)
    abilityBodyPhysics.correctBodyPositions(areaId, gameState)
  }

  private def handleEvents(
      eventsToBeProcessed: List[PhysicsEvent],
      areaId: AreaId,
      gameState: GameState
  ): Unit = {
    eventsToBeProcessed.foreach {
      case TeleportEvent(creatureId, pos) =>
        if (
          gameState.creatures.contains(creatureId) && gameState
            .creatures(creatureId)
            .params
            .currentAreaId == areaId
        ) {
          creatureBodyPhysics.setBodyPos(creatureId, pos)
        }
      case MakeBodySensorEvent(creatureId) =>
        if (
          gameState.creatures.contains(creatureId) && gameState
            .creatures(creatureId)
            .params
            .currentAreaId == areaId
        ) {
          creatureBodyPhysics.setSensor(creatureId)
        }
      case MakeBodyNonSensorEvent(creatureId) =>
        if (
          gameState.creatures.contains(creatureId) && gameState
            .creatures(creatureId)
            .params
            .currentAreaId == areaId
        ) {
          creatureBodyPhysics.setNonSensor(creatureId)
        }
      case _ =>
    }

    eventQueue = eventQueue.filter(!eventsToBeProcessed.contains(_))
  }

  def pollCollisionEvents(): List[GameStateEvent] = {
    val collisionEvents = List().appendedAll(collisionQueue)

    collisionQueue = List()

    collisionEvents
  }

  def scheduleEvents(events: List[PhysicsEvent]): Unit = {
    eventQueue = eventQueue.appendedAll(events)
  }

  def scheduleCollisions(collisions: List[GameStateEvent]): Unit = {
    collisionQueue = collisionQueue.appendedAll(collisions)
  }

  def areaWorlds: Map[AreaId, AreaWorld] = _areaWorlds

  def creatureBodyPositions: Map[EntityId[Creature], Vector2] =
    creatureBodyPhysics.creatureBodyPositions

  def abilityBodyPositions: Map[EntityId[Ability], Vector2] =
    abilityBodyPhysics.abilityBodyPositions
}
