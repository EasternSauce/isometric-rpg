package com.mygdx.game.screen

import com.badlogic.gdx.utils.ScreenUtils
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.view.tile.{Area, Tile}
import com.mygdx.game.view.{CreatureRenderer, Renderable}

case class View() {
  private val area: Area = Area()
  private var creatureRenderers: Map[String, CreatureRenderer] = _

  def init(clientInformation: ClientInformation, gameState: GameState): Unit = {
    creatureRenderers = Map(
      clientInformation.clientCreatureId -> CreatureRenderer(
        clientInformation.clientCreatureId
      )
    )

    creatureRenderers.values.foreach(_.init(gameState))
  }

  def draw(
      viewport: Viewport,
      batch: SpriteBatch,
      gameState: GameState
  ): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    viewport.setProjectionMatrix(batch)

    batch.begin()

    area.baseTiles
      .sorted((tileA: Tile, tileB: Tile) => {
        if (tileA.x == tileB.x) {
          tileB.y - tileA.y
        } else {
          tileB.x - tileA.x
        }
      })
      .foreach(_.render(batch, gameState))

    val creatureRenderables =
      gameState.creatures.keys.map(creatureId => creatureRenderers(creatureId))

    val overgroundRenderables = area.overgroundTiles ++ creatureRenderables

    overgroundRenderables
      .sorted((renderableA: Renderable, renderableB: Renderable) => {
        val (ax, ay) = renderableA.pos(gameState)
        val (bx, by) = renderableB.pos(gameState)
        if (ay == by) {
          bx.compare(ax)
        } else {
          by.compare(ay)
        }
      })
      .foreach(_.render(batch, gameState))

    batch.end()
  }

  def update(
      clientInformation: ClientInformation,
      viewport: Viewport,
      gameState: GameState
  ): Unit = {
    viewport.updateCamera(clientInformation.clientCreatureId, gameState)
  }

}
