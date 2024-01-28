package com.mygdx.game.gamestate

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.input.Input
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{SimpleTimer, WorldDirection}
import com.mygdx.game.view.{CreatureAnimationType, IsometricProjection}
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

import scala.util.chaining.scalaUtilChainingOps

case class Creature(
    params: CreatureParams
) extends Entity {
  def update(
      newX: Float,
      newY: Float,
      delta: Float,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    val vectorTowardsDest =
      new Vector2(
        params.destinationX - params.x,
        params.destinationY - params.y
      )

    if (vectorTowardsDest.len() > 0.2f) {
      vectorTowardsDest.setLength(params.baseVelocity)
    } else {
      vectorTowardsDest.setLength(0f)
    }

    this
      .pipe(creature => {
        if (creature.params.player) {
          val input = Input.poll()

          updatePlayerMovement(input, newX, newY)
        } else {
          val player = gameState.creatures(clientInformation.clientCreatureId)

          creature
            .modify(_.params.x)
            .setTo(newX)
            .modify(_.params.y)
            .setTo(newY)
            .pipe(creature => {
              val distanceToPlayer = Math
                .sqrt(
                  Math.pow(player.params.x - params.x, 2) + Math.pow(
                    player.params.y - params.y,
                    2
                  )
                )
                .toFloat
              if (distanceToPlayer > 1) {
                creature
                  .modify(_.params.destinationX)
                  .setTo(player.params.x)
                  .modify(_.params.destinationY)
                  .setTo(player.params.y)
              } else {
                creature
                  .modify(_.params.destinationX)
                  .setTo(creature.params.x)
                  .modify(_.params.destinationY)
                  .setTo(creature.params.y)
              }
            })
        }
      })
      .modify(_.params.animationTimer)
      .using(_.update(delta))
      .modify(_.params.lastPosTimer)
      .using(_.update(delta))
      .modify(_.params.attackTimer)
      .using(_.update(delta))
      .modify(_.params.velocityX)
      .setTo(vectorTowardsDest.x)
      .modify(_.params.velocityY)
      .setTo(vectorTowardsDest.y)
      .modify(_.params.lastVelocityX)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.x)
      .modify(_.params.lastVelocityY)
      .setToIf(vectorTowardsDest.len() > 0)(vectorTowardsDest.y)
      .pipe(creature => {
        if (creature.params.lastPosTimer.time > 0.5f) {
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
            .pipe(creature => {
              val v1 =
                new Vector2(creature.params.lastPosX, creature.params.lastPosY)
              val v2 = new Vector2(creature.params.x, creature.params.y)

              if (v1.dst(v2) < 0.2f) {
                creature.forceStopMoving()

              } else {
                creature
              }
            })
            .modify(_.params.lastPosX)
            .setTo(params.x)
            .modify(_.params.lastPosY)
            .setTo(params.y)
        } else {
          creature
        }
      })
  }

  private def updatePlayerMovement(
      input: Input,
      playerPosX: Float,
      playerPosY: Float
  ): Creature = {
    val (mouseX: Float, mouseY: Float) = input.mousePos

    val (worldMouseX, worldMouseY) =
      IsometricProjection.translateScreenToIso(mouseX, mouseY)

    val (destinationX, destinationY) =
      (playerPosX + worldMouseX, playerPosY + worldMouseY)

    this
      .modify(_.params.x)
      .setTo(playerPosX)
      .modify(_.params.y)
      .setTo(playerPosY)
      .pipe(creature =>
        if (input.moveButtonPressed) {
          creature
            .modify(_.params.destinationX)
            .setTo(destinationX)
            .modify(_.params.destinationY)
            .setTo(destinationY)
            .modify(_.params.attackTimer)
            .usingIf(creature.params.attackTimer.isRunning)(_.stop())
        } else {
          creature
            .modify(_.params.destinationX)
            .setTo(creature.params.x)
            .modify(_.params.destinationY)
            .setTo(creature.params.y)
        }
      )
      .pipe(creature =>
        if (
          input.attackButtonJustPressed && !input.moveButtonPressed &&
          (!creature.params.attackTimer.isRunning ||
            creature.params.attackTimer.time >= Constants.AttackFrameCount * Constants.AttackFrameDuration + Constants.AttackCooldown)
        ) {
          creature
            .forceStopMoving()
            .modify(_.params.attackTimer)
            .using(_.restart())
        } else {
          creature
        }
      )
  }

  def forceStopMoving(): Creature = {
    this
      .modify(_.params.destinationX)
      .setTo(params.x)
      .modify(_.params.destinationY)
      .setTo(params.y)
  }

  def facingDirection: WorldDirection = {
    val angleDeg =
      new Vector2(params.lastVelocityX, params.lastVelocityY)
        .angleDeg()

    angleDeg match {
      case angle if angle >= 67.5 && angle < 112.5  => WorldDirection.East
      case angle if angle >= 112.5 && angle < 157.5 => WorldDirection.NorthEast
      case angle if angle >= 157.5 && angle < 202.5 => WorldDirection.North
      case angle if angle >= 202.5 && angle < 247.5 => WorldDirection.NorthWest
      case angle if angle >= 247.5 && angle < 292.5 => WorldDirection.West
      case angle if angle >= 292.5 && angle < 337.5 => WorldDirection.SouthWest
      case angle
          if (angle >= 337.5 && angle < 360) || (angle >= 0 && angle < 22.5) =>
        WorldDirection.South
      case angle if angle >= 22.5 && angle < 67.5 => WorldDirection.SouthEast
      case _                                      => throw new RuntimeException("unreachable")
    }
  }

  def moving: Boolean = params.velocityX != 0 && params.velocityY != 0

  def alive: Boolean = true
}

object Creature {
  def male1(
      creatureId: EntityId[Creature],
      x: Float,
      y: Float,
      player: Boolean,
      baseVelocity: Float
  ): Creature = {
    Creature(
      CreatureParams(
        id = creatureId,
        x = x,
        y = y,
        velocityX = 0,
        velocityY = 0,
        destinationX = x,
        destinationY = y,
        lastVelocityX = 0,
        lastVelocityY = 0,
        lastPosX = x,
        lastPosY = y,
        textureNames = Map(
          CreatureAnimationType.Body -> "steel_armor",
          CreatureAnimationType.Head -> "male_head1",
          CreatureAnimationType.Weapon -> "greatstaff",
          CreatureAnimationType.Shield -> "shield"
        ),
        animationTimer = SimpleTimer(isRunning = true),
        lastPosTimer = SimpleTimer(isRunning = true),
        attackTimer = SimpleTimer(isRunning = false),
        player = player,
        baseVelocity = baseVelocity
      )
    )
  }
}
