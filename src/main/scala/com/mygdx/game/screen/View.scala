package com.mygdx.game.screen

import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.utils.ScreenUtils
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.view.tile.Cell
import com.mygdx.game.view.{CreatureRenderer, Renderable}

case class View() {
  var tiledMap: TiledMap = _
  private var creatureRenderers: Map[String, CreatureRenderer] = _

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
//    tiledMapRenderer = new IsometricTiledMapRenderer(tiledMap)
  }

  def draw(
      viewport: Viewport,
      batch: SpriteBatch,
      gameState: GameState
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    viewport.setProjectionMatrix(batch)

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
      Math.abs(-x + y + getMapWidth()) / Math.sqrt(2).toFloat
    }

    overgroundRenderables
      .sorted((renderableA: Renderable, renderableB: Renderable) => {
        val (ax, ay) = renderableA.pos(gameState)
        val (bx, by) = renderableB.pos(gameState)

        distanceFromCameraPlane(bx, by).compare(distanceFromCameraPlane(ax, ay))
      })
      .foreach(_.render(batch, gameState))

    batch.end()
  }

  private def getLayerCells(layerId: Int): List[Renderable] = {
    val layer: TiledMapTileLayer =
      tiledMap.getLayers.get(layerId).asInstanceOf[TiledMapTileLayer]

    (for {
      row <- (0 until layer.getHeight).toList.reverse
      col <- 0 until layer.getWidth
    } yield {
      Option(layer.getCell(col, row)).map(Cell(_, col, row))
    }).flatten
  }

  private def getMapWidth(): Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def update(
      clientInformation: ClientInformation,
      viewport: Viewport,
      gameState: GameState
  ): Unit = {
    viewport.updateCamera(clientInformation.clientCreatureId, gameState)
  }

  private def getMapHeight(): Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

}
