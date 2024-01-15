package com.mygdx.game.view.tile

import scala.util.Random

case class Area() {
  val baseTiles: List[Tile] = fillGroundTiles(15, 15)
  val overgroundTiles: List[Tile] = {
    (for {
      i <- (0 until 15).toList
      j <- (0 until 15).toList
    } yield {
      if (Random.nextInt(100) < 5) {
        Some(Tile(j, i, TileType.Tree))
      } else {
        None
      }
    }).flatten
  }

  private def fillGroundTiles(width: Int, height: Int) = {
    for {
      i <- (0 until height).toList
      j <- (0 until width).toList
    } yield {
      Tile(j, i, TileType.Ground)
    }
  }
}
