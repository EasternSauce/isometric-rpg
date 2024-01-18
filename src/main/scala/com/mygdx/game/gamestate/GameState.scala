package com.mygdx.game.gamestate

import com.mygdx.game.screen.{ClientInformation, KeyboardInput}
import com.mygdx.game.util.{SimpleTimer, WorldDirection}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class GameState(
    creatures: Map[String, Creature]
) {
  def update(
      clientInformation: ClientInformation,
      keyboardInput: KeyboardInput,
      playerPosX: Float,
      playerPosY: Float,
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.at(clientInformation.clientCreatureId))
      .using(
        GameState.updatePlayerMovement(keyboardInput, playerPosX, playerPosY)
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
        lastVelocityX = 0,
        lastVelocityY = 0,
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
        animationTimer = SimpleTimer(isRunning = true)
      )
    )

    GameState(creatures =
      Map(
        clientInformation.clientCreatureId ->
          creature
      )
    )
  }

  def updatePlayerMovement(
      keyboardInput: KeyboardInput,
      playerPosX: Float,
      playerPosY: Float
  ): Creature => Creature = {
    val baseVelocity = 3f

    val velocity = if (keyboardInput.movingDiagonally()) {
      baseVelocity / Math.sqrt(2).toFloat
    } else {
      baseVelocity
    }
    val velocityX =
      (keyboardInput.moveNorth, keyboardInput.moveSouth) match {
        case (true, false) => -velocity
        case (false, true) => velocity
        case _             => 0
      }

    val velocityY =
      (keyboardInput.moveWest, keyboardInput.moveEast) match {
        case (true, false) => -velocity
        case (false, true) => velocity
        case _             => 0
      }

    creature => {
      creature
        .modify(_.params.x)
        .setTo(playerPosX)
        .modify(_.params.y)
        .setTo(playerPosY)
        .modify(_.params.velocityX)
        .setTo(velocityX)
        .modify(_.params.velocityY)
        .setTo(velocityY)
        .modify(_.params.lastVelocityX)
        .setToIf(velocityX != 0 && velocityY != 0)(velocityX)
        .modify(_.params.lastVelocityY)
        .setToIf(velocityX != 0 && velocityY != 0)(velocityY)
    }
  }

}
