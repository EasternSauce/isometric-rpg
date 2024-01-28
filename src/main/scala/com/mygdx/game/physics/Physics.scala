package com.mygdx.game.physics

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.{Creature, EntityId, GameState}
import com.mygdx.game.levelmap.LevelMap

case class Physics() {
  private var world: World = _
  private var creatureBodies: Map[EntityId[Creature], CreatureBody] = _
  private var staticBodies: List[PhysicsBody] = _
  private var clientInformation: ClientInformation = _

  def init(
      clientInformation: ClientInformation,
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {
    world = World()
    world.init()

    val player = gameState.creatures(clientInformation.clientCreatureId)
    val playerBody = CreatureBody(clientInformation.clientCreatureId)
    playerBody.init(world, player.params.x, player.params.y)

    creatureBodies = Map(clientInformation.clientCreatureId -> playerBody)

    this.clientInformation = clientInformation

    val cells = levelMap.getLayerCells(0) ++ levelMap.getLayerCells(1)

    val borders =
      (0 until levelMap.getMapWidth).zip(LazyList.continually(0)) ++
        LazyList.continually(0).zip(0 until levelMap.getMapHeight) ++
        LazyList
          .continually(levelMap.getMapWidth)
          .zip(0 until levelMap.getMapHeight - 1) ++
        (0 until levelMap.getMapWidth).zip(
          LazyList.continually(levelMap.getMapHeight - 1)
        )

    staticBodies =
      cells.filterNot(_.walkable).map(_.pos(gameState)).distinct.map {
        case (x, y) =>
          val terrainBody = TerrainBody("terrainBody_" + x + "_" + y)
          terrainBody.init(world, x, y)
          terrainBody
      } ++ borders.map { case (x, y) =>
        val borderBody = BorderBody("borderBody_" + x + "_" + y)
        borderBody.init(world, x, y)
        borderBody
      }
  }

  def update(gameState: GameState): Unit = {
    world.update()

    val bodiesToCreate =
      gameState.creatures.keys.toSet -- creatureBodies.keys.toSet

    bodiesToCreate.foreach(creatureId => {
      val creatureBody = CreatureBody(creatureId)

      val creature = gameState.creatures(creatureId)

      creatureBody.init(world, creature.params.x, creature.params.y)
      creatureBodies = creatureBodies.updated(creatureId, creatureBody)
    })

    creatureBodies.values.foreach(_.update(gameState))
  }

  def getCreaturePositions: Map[EntityId[Creature], (Float, Float)] = {
    creatureBodies.values
      .map(creatureBody => {
        val (x, y) = creatureBody.getPos
        (creatureBody.creatureId, (x, y))
      })
      .toMap
  }

  def getWorld: World = world

}
