package com.mygdx.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.{Sprite, SpriteBatch}
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.Random

object GameplayScreen extends Screen {

  val baseTiles: List[Tile] = {
    for {
      i <- (0 until 15).toList
      j <- (0 until 15).toList
    } yield {
      Tile(TilePos(j, i), TileType.Ground)
    }
  }
  val overgroundTiles: List[Tile] = {
    (for {
      i <- (0 until 15).toList
      j <- (0 until 15).toList
    } yield {
      if (Random.nextInt(100) < 5) {
        Some(Tile(TilePos(j, i), TileType.Tree))
      } else {
        None
      }
    }).flatten
  }
  var gameState: GameState = _
  var worldViewport: Viewport = _
  var worldCamera: OrthographicCamera = _
  var sprite: Sprite = _
  var creatureRenderers: Map[String, CreatureRenderer] = _
  protected var batch: SpriteBatch = _

  override def show(): Unit = {
    val creature = Creature(
      id = "creature1",
      x = 0,
      y = 0,
      textureName = "wanderer",
      neutralStanceFrame = 0,
      frameCount = 3,
      frameDuration = 0.2f,
      dirMap = Map(
        WorldDirection.South -> 0,
        WorldDirection.East -> 1,
        WorldDirection.North -> 2,
        WorldDirection.West -> 3
      ),
      animationTimer = SimpleTimer(isRunning = true),
      moving = false,
      lastMovementDir = (0, 0)
    )

    gameState = GameState(creatures =
      Map(
        "creature1" ->
          creature
      )
    )

    batch = new SpriteBatch()

    worldCamera = new OrthographicCamera()

    worldViewport = new FitViewport(
      Constants.ViewpointWorldWidth,
      Constants.ViewpointWorldHeight,
      worldCamera
    )

    creatureRenderers = Map("creature1" -> CreatureRenderer("creature1"))

    creatureRenderers.values.foreach(_.init(gameState))
  }

  override def render(delta: Float): Unit = {
    updateGameState(delta)

    updateView(gameState)

    drawView()
  }

  private def drawView(): Unit = {
    ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1)

    batch.setProjectionMatrix(worldCamera.combined)

    batch.begin()

    baseTiles
      .sorted((tileA: Tile, tileB: Tile) => {
        if (tileA.tilePos.x == tileB.tilePos.x) {
          tileB.tilePos.y - tileA.tilePos.y
        } else {
          tileB.tilePos.x - tileA.tilePos.x
        }
      })
      .foreach(_.render(batch, gameState))

    val creatureRenderables =
      gameState.creatures.keys.map(creatureId => creatureRenderers(creatureId))

    val overgroundRenderables = overgroundTiles ++ creatureRenderables

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

  private def updateView(gameState: GameState): Unit = {
    updateCamera(gameState)
  }

  def updateCamera(gameState: GameState): Unit = {

    val camPosition = worldCamera.position

    val creature = gameState.creatures("creature1")
    val (x, y) = Tile.convertIsometricCoordinates(creature.x, creature.y)

    camPosition.x = (math.floor(x * 100) / 100).toFloat
    camPosition.y = (math.floor(y * 100) / 100).toFloat

    worldCamera.update()

  }

  def updateGameState(delta: Float): Unit = {
    import com.badlogic.gdx.Gdx.input

    val (left, right, down, up) = (
      input.isKeyPressed(Keys.A),
      input.isKeyPressed(Keys.D),
      input.isKeyPressed(Keys.S),
      input.isKeyPressed(Keys.W)
    )

    val diagonalMovement = (left || right) && (up || down)

    val baseSpeed = 3f

    val speed = if (diagonalMovement) {
      delta * baseSpeed / Math.sqrt(2).toFloat
    } else {
      delta * baseSpeed
    }

    val deltaX =
      (left, right) match {
        case (true, false) => -speed
        case (false, true) => speed
        case _             => 0
      }

    val deltaY =
      (down, up) match {
        case (true, false) => -speed
        case (false, true) => speed
        case _             => 0
      }

    gameState = gameState
      .modify(_.creatures.at("creature1").moving)
      .setTo(false)
      .modify(_.creatures.at("creature1"))
      .using { creature =>
        creature
          .modify(_.x)
          .setTo(creature.x + deltaX)
          .modify(_.moving)
          .setToIf(deltaX != 0)(true)
      }
      .modify(_.creatures.at("creature1"))
      .using { creature =>
        creature
          .modify(_.y)
          .setTo(creature.y + deltaY)
          .modify(_.moving)
          .setToIf(deltaY != 0)(true)
      }
      .modify(_.creatures.at("creature1").lastMovementDir)
      .setToIf(deltaX != 0 || deltaY != 0)((deltaX, deltaY))
      .modify(_.creatures.each)
      .using(_.update(delta))
  }

  override def dispose(): Unit = {
    batch.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    worldViewport.update(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
