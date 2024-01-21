package com.mygdx.game.screen

import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.mygdx.game.view.tile.CellRenderer

case class LevelMap() {
  private var tiledMap: TiledMap = _

  def init(): Unit = {
    tiledMap = new TmxMapLoader().load("assets/tiled/map1.tmx")

    tiledMap.getTileSets.forEach(
      _.forEach(
        _.getTextureRegion.getTexture
          .setFilter(TextureFilter.Linear, TextureFilter.Linear)
      )
    )
  }

  def getMapWidth: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getWidth
  }

  def getMapHeight: Int = {
    tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer].getHeight
  }

  def getLayerCells(layerId: Int): List[CellRenderer] = {
    val layer: TiledMapTileLayer =
      tiledMap.getLayers.get(layerId).asInstanceOf[TiledMapTileLayer]

    val cells: List[Option[CellRenderer]] = for {
      row <- (0 until layer.getHeight).toList.reverse
      col <- 0 until layer.getWidth
    } yield {
      Option(layer.getCell(col, row)).map(CellRenderer(_, col, row))
    }

    cells.flatten
  }
}
