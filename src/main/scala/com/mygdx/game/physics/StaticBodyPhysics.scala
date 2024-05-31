package com.mygdx.game.physics

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.Cell

case class StaticBodyPhysics() {

  private var staticBodies: List[PhysicsBody] = _
  private var world: World = _

  def init(tiledMap: TiledMap, world: World, gameState: GameState): Unit = {
    this.world = world

    val terrainCollisions =
      getTerrainCollisionCells(tiledMap) ++ getBigObjectCells(tiledMap)

    val objectCollisions = getObjectCollisionCells(tiledMap)

    staticBodies =
      createStaticBodies(terrainCollisions, objectCollisions, gameState)
  }

  private def createStaticBodies(
      terrainCollisions: List[Cell],
      objectCollisions: List[Cell],
      gameState: GameState
  ): List[PhysicsBody] = {
    createTerrainBodies(terrainCollisions, gameState) ++ createObjectBodies(
      objectCollisions,
      gameState
    )
  }

  private def createObjectBodies(
      objectCollisions: List[Cell],
      gameState: GameState
  ): List[ObjectBody] = {
    objectCollisions
      .map(_.pos(gameState))
      .distinct
      .map(createObjectBody(gameState, _))
  }

  private def createTerrainBodies(
      terrainCollisions: List[Cell],
      gameState: GameState
  ): List[TerrainBody] = {
    terrainCollisions
      .map(_.pos(gameState))
      .distinct
      .map(
        createTerrainBody(gameState, _)
      )
  }

  private def createObjectBody(gameState: GameState, pos: Vector2) = {
    val objectBody = ObjectBody("objectBody_" + pos.x + "_" + pos.y)
    objectBody.init(world, pos, gameState)
    objectBody
  }

  private def createTerrainBody(gameState: GameState, pos: Vector2) = {
    val terrainBody = TerrainBody("terrainBody_" + pos.x + "_" + pos.y)
    terrainBody.init(world, pos, gameState)
    terrainBody
  }

  private def getBigObjectCells(tiledMap: TiledMap) = {
    tiledMap.getLayer("object")
  }

  private def getObjectCollisionCells(tiledMap: TiledMap) = {
    tiledMap
      .getLayer("collision")
      .filter(_.cell.getTile.getId == Constants.smallObjectCollisionCellId) ++
      tiledMap
        .getLayer("manual_collision")
        .filter(_.cell.getTile.getId == Constants.smallObjectCollisionCellId)
  }

  private def getTerrainCollisionCells(tiledMap: TiledMap) = {
    tiledMap
      .getLayer("collision")
      .filter(cell =>
        cell.cell.getTile.getId == Constants.waterGroundCollisionCellId
          || cell.cell.getTile.getId == Constants.bigObjectCollisionCellId
          || cell.cell.getTile.getId == Constants.wallCollisionCellId
      ) ++
      tiledMap
        .getLayer("manual_collision")
        .filter(_.cell.getTile.getId == Constants.waterGroundCollisionCellId)
  }
}
