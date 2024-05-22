package com.mygdx.game.tiledmap

import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader, TiledMap => GdxTiledMap}
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.TileRenderer

case class TiledMap() {
  private var gdxTiledMap: GdxTiledMap = _

  private var layers: Map[Int, List[TileRenderer]] = _

  def init(): Unit = {
    val params = new TmxMapLoader.Parameters()

    gdxTiledMap = new TmxMapLoader().load("assets/maps/map2/map2.tmx", params)

    layers = Map(
      0 -> loadLayerCells(0),
      1 -> loadLayerCells(1),
      2 -> loadLayerCells(2)
    )

  }

  def getMapWidth: Int = {
    gdxTiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def getMapHeight: Int = {
    gdxTiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

  private def loadLayerCells(layerId: Int): List[TileRenderer] = {
    val layer: TiledMapTileLayer =
      gdxTiledMap.getLayers.get(layerId).asInstanceOf[TiledMapTileLayer]

    val cells: List[Option[TileRenderer]] = for {
      x <- (0 until layer.getWidth).toList
      y <- (0 until layer.getHeight).reverse
    } yield {
      Option(layer.getCell(x, y)).map(TileRenderer(_, Vector2(x, y)))
    }

    cells.flatten
  }

  def getLayer(layerId: Int): List[TileRenderer] = layers(layerId)
}
