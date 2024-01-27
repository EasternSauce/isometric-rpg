package com.mygdx.game.gamestate

import com.badlogic.gdx.Gdx
import com.mygdx.game.input.Input
import com.mygdx.game.util.SimpleTimer
import com.mygdx.game.view.{CreatureAnimationType, IsometricProjection}
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creature: Creature,
    creatures: Map[EntityId[Creature], Creature]
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
          CreatureAnimationType.Head -> "male_head1",
          CreatureAnimationType.Weapon -> "greatstaff",
          CreatureAnimationType.Shield -> "shield"
        ),
        neutralStanceFrame = 0,
        animationTimer = SimpleTimer(isRunning = true),
        lastPosTimer = SimpleTimer(isRunning = true),
        attackTimer = SimpleTimer(isRunning = false)
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

      val attackButtonJustPressed =
        Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.RIGHT)

      val moveButtonPressed =
        Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)

      creature
        .modify(_.params.x)
        .setTo(playerPosX)
        .modify(_.params.y)
        .setTo(playerPosY)
        .pipe(creature =>
          if (moveButtonPressed) {
            creature
              .modify(_.params.destinationX)
              .setTo(destinationX)
              .modify(_.params.destinationY)
              .setTo(destinationY)
              .modify(_.params.attackTimer)
              .usingIf(creature.params.attackTimer.isRunning)(_.stop())
          } else creature
        )
        .pipe(creature =>
          if (
            attackButtonJustPressed && !moveButtonPressed && (!creature.params.attackTimer.isRunning || creature.params.attackTimer.time >= Constants.AttackFrameCount * Constants.AttackFrameDuration + Constants.AttackCooldown)
          ) {
            creature
              .forceStopMoving()
              .modify(_.params.attackTimer)
              .using(_.restart())
          } else { creature }
        )

    }
  }

}
