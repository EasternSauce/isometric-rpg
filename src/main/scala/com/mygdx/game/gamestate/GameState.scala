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
      delta: Float
  ): GameState = {
    this
      .modify(_.creatures.at(clientInformation.clientCreatureId))
      .using(GameState.updatePlayerMovement(keyboardInput, delta))
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
      delta: Float
  ): Creature => Creature = {
    val baseSpeed = 3f

    val speed = if (keyboardInput.movingDiagonally()) {
      delta * baseSpeed / Math.sqrt(2).toFloat
    } else {
      delta * baseSpeed
    }

    val deltaX =
      (keyboardInput.moveNorth, keyboardInput.moveSouth) match {
        case (true, false) => -speed
        case (false, true) => speed
        case _             => 0
      }

    val deltaY =
      (keyboardInput.moveWest, keyboardInput.moveEast) match {
        case (true, false) => -speed
        case (false, true) => speed
        case _             => 0
      }

    creature => {
      creature
        .modify(_.params.moving)
        .setTo(false)
        .modify(_.params.x)
        .setTo(creature.params.x + deltaX)
        .modify(_.params.moving)
        .setToIf(deltaX != 0)(true)
        .modify(_.params.y)
        .setTo(creature.params.y + deltaY)
        .modify(_.params.moving)
        .setToIf(deltaY != 0)(true)
        .modify(_.params.lastMovementDir)
        .setToIf(deltaX != 0 || deltaY != 0)((deltaX, deltaY))
    }
  }

}
