package com.mygdx.game.gamestate

import com.badlogic.gdx.Gdx
import com.mygdx.game.screen.{ClientInformation, Input}
import com.mygdx.game.util.{SimpleTimer, WorldDirection}
import com.mygdx.game.view.tile.Tile
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class GameState(
    creature: Creature,
    creatures: Map[String, Creature]
) {
  def update(
      clientInformation: ClientInformation,
      playerPosX: Float,
      playerPosY: Float,
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.at(clientInformation.clientCreatureId))
      .using(
        GameState.updatePlayerMovement(
          playerPosX,
          playerPosY
        )
      )
      .modify(_.creatures.each)
      .using(_.update(delta))
  }
}

object GameState {
  def initialState(clientInformation: ClientInformation): GameState = {
    val creature = Creature(
      CreatureParams(
        id = clientInformation.clientCreatureId,
        x = 0,
        y = 0,
        velocityX = 0,
        velocityY = 0,
        destinationX = 0,
        destinationY = 0,
        lastVelocityX = 0,
        lastVelocityY = 0,
        lastPosX = 0,
        lastPosY = 0,
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
        lastPosTimer = SimpleTimer(isRunning = true)
      )
    )

    GameState(
      creature = creature,
      creatures = Map(
        clientInformation.clientCreatureId ->
          creature
      )
    )
  }

  def updatePlayerMovement(
      playerPosX: Float,
      playerPosY: Float
  ): Creature => Creature = { creature =>
    {
      val (mouseX: Float, mouseY: Float) = Input.getMousePos

      val (worldMouseX, worldMouseY) = Tile.translateScreenToIso(mouseX, mouseY)

      val (destinationX, destinationY) =
        (playerPosX + worldMouseX, playerPosY + worldMouseY)

      val mouseButtonDown = Gdx.input.isTouched()

      creature
        .modify(_.params.x)
        .setTo(playerPosX)
        .modify(_.params.y)
        .setTo(playerPosY)
        .modify(_.params.destinationX)
        .setToIf(mouseButtonDown)(destinationX)
        .modify(_.params.destinationY)
        .setToIf(mouseButtonDown)(destinationY)
    }
  }

}
