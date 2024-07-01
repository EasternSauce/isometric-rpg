package com.mygdx.game.physics

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.tiledmap.TiledMap
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.Cell

case class StaticBodyPhysics() {

  private var staticBodies: List[PhysicsBody] = _
  private var areaWorlds: Map[AreaId, AreaWorld] = _

  def init(
      tiledMaps: Map[AreaId, TiledMap],
      areaWorlds: Map[AreaId, AreaWorld],
      gameState: GameState
  ): Unit = {

    staticBodies = List()

    areaWorlds.foreach { case (areaId, world) =>
      val tiledMap = tiledMaps(areaId)

      val terrainCollisions =
        getTerrainCollisionCells(tiledMap) ++ getBigObjectCells(tiledMap)

      val objectCollisions = getObjectCollisionCells(tiledMap)

      staticBodies ++= createStaticBodies(
        terrainCollisions,
        objectCollisions,
        world,
        gameState
      )
    }

    this.areaWorlds = areaWorlds
  }

  private def createStaticBodies(
      terrainCollisions: List[Cell],
      objectCollisions: List[Cell],
      world: AreaWorld,
      gameState: GameState
  ): List[PhysicsBody] = {
    createTerrainBodies(terrainCollisions, world, gameState) ++
      createObjectBodies(objectCollisions, world, gameState)
  }

  private def createObjectBodies(
      objectCollisions: List[Cell],
      world: AreaWorld,
      gameState: GameState
  ): List[ObjectBody] = {
    objectCollisions
      .map(_.pos(gameState))
      .distinct
      .map(createObjectBody(_, world, gameState))
  }

  private def createTerrainBodies(
      terrainCollisions: List[Cell],
      world: AreaWorld,
      gameState: GameState
  ): List[TerrainBody] = {
    terrainCollisions
      .map(_.pos(gameState))
      .distinct
      .map(createTerrainBody(_, world, gameState))
  }

  private def createObjectBody(
      pos: Vector2,
      world: AreaWorld,
      gameState: GameState
  ) = {
    val objectBody = ObjectBody("objectBody_" + pos.x + "_" + pos.y)
    objectBody.init(world, pos, gameState)
    objectBody
  }

  private def createTerrainBody(
      pos: Vector2,
      world: AreaWorld,
      gameState: GameState
  ) = {
    val terrainBody = TerrainBody("terrainBody_" + pos.x + "_" + pos.y)
    terrainBody.init(world, pos, gameState)
    terrainBody
  }

  private def getBigObjectCells(tiledMap: TiledMap): List[Cell] = {
    tiledMap.getLayer("object")
  }

  private def getObjectCollisionCells(tiledMap: TiledMap) = {
    tiledMap
      .getLayer("collision")
      .filter(
        _.tiledCell.getTile.getId == Constants.smallObjectCollisionCellId
      ) ++
      tiledMap
        .getLayer("manual_collision")
        .filter(
          _.tiledCell.getTile.getId == Constants.smallObjectCollisionCellId
        )
  }

  private def getTerrainCollisionCells(tiledMap: TiledMap): List[Cell] = {
    tiledMap
      .getLayer("collision")
      .filter(cell => {
        val cellId = cell.tiledCell.getTile.getId
        cellId == Constants.waterGroundCollisionCellId ||
        cellId == Constants.bigObjectCollisionCellId ||
        cellId == Constants.wallCollisionCellId
      }) ++
      tiledMap
        .getLayer("manual_collision")
        .filter(
          _.tiledCell.getTile.getId == Constants.waterGroundCollisionCellId
        )
  }
}
