package com.mygdx.game.gamestate

import com.badlogic.gdx.Gdx
import com.mygdx.game.ClientInformation
import com.mygdx.game.input.Input
import com.mygdx.game.util.SimpleTimer
import com.mygdx.game.view.{CreatureAnimationType, IsometricProjection}
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
        x = 5,
        y = 5,
        velocityX = 0,
        velocityY = 0,
        destinationX = 5,
        destinationY = 5,
        lastVelocityX = 0,
        lastVelocityY = 0,
        lastPosX = 5,
        lastPosY = 5,
        textureNames = Map(
          CreatureAnimationType.Body -> "steel_armor",
          CreatureAnimationType.Head -> "male_head1"
        ),
        neutralStanceFrame = 0,
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

      val (worldMouseX, worldMouseY) =
        IsometricProjection.translateScreenToIso(mouseX, mouseY)

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
