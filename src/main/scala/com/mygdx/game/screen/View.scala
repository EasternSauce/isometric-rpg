package com.mygdx.game.screen

import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.utils.ScreenUtils
import com.mygdx.game.gamestate.{Creature, GameState}
import com.mygdx.game.view.tile.{Cell, Tile}
import com.mygdx.game.view.{CreatureRenderer, Renderable}

case class View(clientInformation: ClientInformation) {
  private val worldViewport: Viewport = Viewport()
  private val b2DebugViewport: Viewport = Viewport()
  private val world: World = World()
  private val playerBody: CreatureBody = CreatureBody(
    clientInformation.clientCreatureId
  )
  private var tiledMap: TiledMap = _
  private var creatureRenderers: Map[String, CreatureRenderer] = _
  private var terrainBodies: List[TerrainBody] = List()

  def init(clientInformation: ClientInformation, gameState: GameState): Unit = {
    creatureRenderers = Map(
      clientInformation.clientCreatureId -> CreatureRenderer(
        clientInformation.clientCreatureId
      )
    )

    creatureRenderers.values.foreach(_.init(gameState))

    tiledMap = new TmxMapLoader().load("assets/tiled/map1.tmx")

    tiledMap.getTileSets.forEach(
      _.forEach(
        _.getTextureRegion.getTexture
          .setFilter(TextureFilter.Linear, TextureFilter.Linear)
      )
    )

    world.init()

    playerBody.init(world, 0, 0)

    val layer1Cells = getLayerCells(1)

    terrainBodies = layer1Cells.map(_.pos(gameState)).map { case (x, y) =>
      TerrainBody("terrainBody_" + x + "_" + y, x, y)
    }

    terrainBodies.foreach(terrainBody =>
      terrainBody.init(world, terrainBody.x, terrainBody.y)
    )

    worldViewport.init(1, (x, y) => Tile.translateIsoToScreen(x, y))
    b2DebugViewport.init(0.02f, (x, y) => (x, y))
  }

  private def getLayerCells(layerId: Int): List[Renderable] = {
    val layer: TiledMapTileLayer =
      tiledMap.getLayers.get(layerId).asInstanceOf[TiledMapTileLayer]

    val cells: List[Option[Cell]] = for {
      row <- (0 until layer.getHeight).toList.reverse
      col <- 0 until layer.getWidth
    } yield {
      Option(layer.getCell(col, row)).map(Cell(_, col, row))
    }

    cells.flatten
  }

  def draw(
      batch: SpriteBatch,
      gameState: GameState
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    worldViewport.setProjectionMatrix(batch)

    batch.begin()

    val layer0Cells = getLayerCells(0)
    val layer1Cells = getLayerCells(1)

    layer0Cells.foreach(_.render(batch, gameState))

    val creatureRenderables =
      gameState.creatures.keys.toList.map(creatureId =>
        creatureRenderers(creatureId)
      )

    val overgroundRenderables = layer1Cells ++ creatureRenderables

    def distanceFromCameraPlane(x: Float, y: Float): Float = {
      Math.abs(-x + y + getMapWidth) / Math.sqrt(2).toFloat
    }

    overgroundRenderables
      .sorted((renderableA: Renderable, renderableB: Renderable) => {
        val (ax, ay) = renderableA.pos(gameState)
        val (bx, by) = renderableB.pos(gameState)

        distanceFromCameraPlane(bx, by).compare(distanceFromCameraPlane(ax, ay))
      })
      .foreach(_.render(batch, gameState))

    batch.end()

    world.renderDebug(b2DebugViewport)
  }

  private def getMapWidth: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def update(
      clientInformation: ClientInformation,
      gameState: GameState
  ): Unit = {
    worldViewport.updateCamera(clientInformation.clientCreatureId, gameState)
    b2DebugViewport.updateCamera(clientInformation.clientCreatureId, gameState)
    world.update()

    val player: Creature =
      gameState.creatures(clientInformation.clientCreatureId)
    playerBody.move(player.params.velocityX, player.params.velocityY)

    playerBody.update()
  }

  def resize(width: Int, height: Int): Unit = {
    worldViewport.update(width, height)
    b2DebugViewport.update(width, height)
  }

  def getPlayerPos: (Float, Float) = {
    playerBody.getPos
  }

  private def getMapHeight: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

}
