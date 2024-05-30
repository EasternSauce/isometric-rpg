package com.mygdx.game.tiledmap

import com.badlogic.gdx.maps.tiled.{TiledMapTileLayer, TmxMapLoader, TiledMap => GdxTiledMap}
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.Cell

case class TiledMap() {
  private var gdxTiledMap: GdxTiledMap = _

  private var layers: Map[String, List[Cell]] = _

  def init(): Unit = {
    val params = new TmxMapLoader.Parameters()

    gdxTiledMap = new TmxMapLoader().load("assets/maps/map2/map2.tmx", params)

    layers = Map(
      "fill" -> loadLayerCells("fill"),
      "background" -> loadLayerCells("background"),
      "object" -> loadLayerCells("object"),
      "manual_object" -> loadLayerCells("manual_object"),
      "collision" -> loadLayerCells("collision"),
      "manual_collision" -> loadLayerCells("manual_collision")
    )
  }

  def getMapWidth: Int = {
    gdxTiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def getMapHeight: Int = {
    gdxTiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

  private def loadLayerCells(layerName: String): List[Cell] = {
    val layer: TiledMapTileLayer =
      gdxTiledMap.getLayers.get(layerName).asInstanceOf[TiledMapTileLayer]

    val cells: List[Option[Cell]] = for {
      x <- (0 until layer.getWidth).toList
      y <- (0 until layer.getHeight).reverse
    } yield {
      Option(layer.getCell(x, y)).map(Cell(_, Vector2(x, y)))
    }

    cells.flatten
  }

  def getLayer(layerName: String): List[Cell] = layers(layerName)
}
