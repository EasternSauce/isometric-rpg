package com.mygdx.game.levelmap

import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.TileRenderer

case class LevelMap() {
  private var tiledMap: TiledMap = _

  def init(): Unit = {
    val params = new TmxMapLoader.Parameters()

    tiledMap = new TmxMapLoader().load("assets/maps/map1.tmx", params)

  }

  def getMapWidth: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def getMapHeight: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

  def getLayerCells(layerId: Int): List[TileRenderer] = {
    val layer: TiledMapTileLayer =
      tiledMap.getLayers.get(layerId).asInstanceOf[TiledMapTileLayer]

    val cells: List[Option[TileRenderer]] = for {
      x <- (0 until layer.getHeight).toList.reverse
      y <- 0 until layer.getWidth
    } yield {
      Option(layer.getCell(x, y)).map(TileRenderer(_, Vector2(x, y)))
    }

    cells.flatten
  }
}
