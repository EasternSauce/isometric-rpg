package com.mygdx.game.physics

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.levelmap.LevelMap

case class Physics() {
  private var world: World = _
  private var playerBody: CreatureBody = _
  private var physicsBodies: List[PhysicsBody] = _
  private var clientInformation: ClientInformation = _

  def init(
      clientInformation: ClientInformation,
      levelMap: LevelMap,
      gameState: GameState
  ): Unit = {

    this.world = World()
    this.playerBody = CreatureBody(clientInformation.clientCreatureId)
    this.clientInformation = clientInformation

    val player = gameState.creatures(clientInformation.clientCreatureId)

    world.init()

    playerBody.init(world, player.params.x, player.params.y)

    val cells = levelMap.getLayerCells(0) ++ levelMap.getLayerCells(1)

    val borders =
      (0 until levelMap.getMapWidth).zip(LazyList.continually(-1)) ++
        LazyList.continually(-1).zip(0 until levelMap.getMapHeight) ++
        LazyList
          .continually(levelMap.getMapWidth)
          .zip(0 until levelMap.getMapHeight) ++
        (0 until levelMap.getMapWidth).zip(
          LazyList.continually(levelMap.getMapHeight)
        )

    physicsBodies =
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

    val player =
      gameState.creatures(clientInformation.clientCreatureId)
    playerBody.move(player.params.velocityX, player.params.velocityY)

    playerBody.update()
  }

  def getPlayerPos: (Float, Float) = {
    playerBody.getPos
  }

  def getWorld: World = world

}
