package com.mygdx.game.gamestate

import com.mygdx.game.input.Input
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{SimpleTimer, Vector2, WorldDirection}
import com.mygdx.game.view.{CreatureAnimationType, IsometricProjection}
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

import scala.util.chaining.scalaUtilChainingOps

case class Creature(
    params: CreatureParams
) extends Entity {
  def update(
      newPos: Vector2,
      delta: Float,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    val vectorTowardsDest = params.pos.vectorTowards(params.destination)

    val velocity = if (!alive) {
      Vector2(0, 0)
    } else if (vectorTowardsDest.length > 0.2f) {
      vectorTowardsDest.withLength(params.baseVelocity)
    } else {
      vectorTowardsDest.withLength(0f)
    }

    this
      .pipe(creature => {
        if (creature.params.player) {
          updatePlayerMovement(input, newPos, gameState)
        } else {
          val player = gameState.creatures(clientInformation.clientCreatureId)

          creature
            .modify(_.params.pos)
            .setTo(newPos)
            .pipe(creature => {
              val distanceToPlayer = Math // TODO: use vector2!
                .sqrt(
                  Math.pow(player.params.pos.x - params.pos.x, 2) + Math.pow(
                    player.params.pos.y - params.pos.y,
                    2
                  )
                )
                .toFloat
              if (distanceToPlayer > 1) {
                creature
                  .modify(_.params.destination)
                  .setTo(player.params.pos)
              } else {
                creature
                  .modify(_.params.destination)
                  .setTo(creature.params.pos)
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
      .modify(_.params.velocity)
      .setTo(velocity)
      .modify(_.params.lastVelocity)
      .setToIf(velocity.length > 0)(velocity)
      .pipe(creature => {
        if (creature.params.lastPosTimer.time > 0.5f) {
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
            .pipe(creature => {
              val v1 = creature.params.lastPos
              val v2 = creature.params.pos

              if (v1.distance(v2) < 0.2f) {
                creature.forceStopMoving()

              } else {
                creature
              }
            })
            .modify(_.params.lastPos)
            .setTo(params.pos)
        } else {
          creature
        }
      })
  }

  private def updatePlayerMovement(
      input: Input,
      playerPos: Vector2,
      gameState: GameState
  ): Creature = {
    val mousePos = input.mousePos

    val mouseScreenPos =
      IsometricProjection.translateScreenToIso(mousePos)

    val mouseWorldPos = playerPos.add(mouseScreenPos)

    this
      .modify(_.params.pos)
      .setTo(playerPos)
      .pipe(creature =>
        if (input.moveButtonPressed) {
          creature
            .modify(_.params.destination)
            .setTo(mouseWorldPos)
            .modify(_.params.attackTimer)
            .usingIf(creature.params.attackTimer.isRunning)(_.stop())
        } else {
          creature
            .modify(_.params.destination)
            .setTo(creature.params.pos)
        }
      )
      .pipe(creature =>
        if (
          input.attackButtonJustPressed && !input.moveButtonPressed &&
          (!creature.params.attackTimer.isRunning ||
            creature.params.attackTimer.time >= Constants.AttackFrameCount * Constants.AttackFrameDuration + Constants.AttackCooldown)
        ) {
          val closestCreature =
            gameState.getAliveCreatureClosestTo(mouseWorldPos, List(params.id))

          if (
            closestCreature.nonEmpty && closestCreature.get.params.pos
              .distance(params.pos) < Constants.AttackRange
          ) {
            creature
              .forceStopMoving()
              .modify(_.params.attackTimer)
              .using(_.restart())
              .attack(closestCreature.get)
          } else {
            creature
              .forceStopMoving()
              .modify(_.params.attackTimer)
              .using(_.restart())
          }

        } else {
          creature
        }
      )
  }

  def attack(otherCreature: Creature): Creature = {
    this
      .modify(_.params.attackedCreatureId)
      .setTo(Some(otherCreature.params.id))
  }

  def forceStopMoving(): Creature = {
    this
      .modify(_.params.destination)
      .setTo(params.pos)
  }

  def alive: Boolean = params.life > 0

  def facingDirection: WorldDirection = {
    val angleDeg = params.lastVelocity.angleDeg

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

  def moving: Boolean = params.velocity.length > 0

  def takeDamage(damage: Float): Creature = {
    if (params.life - damage > 0) {
      this.modify(_.params.life).setTo(params.life - damage)
    } else {
      this.modify(_.params.life).setTo(0)
    }
  }

}

object Creature {
  def male1(
      creatureId: EntityId[Creature],
      pos: Vector2,
      player: Boolean,
      baseVelocity: Float
  ): Creature = {
    Creature(
      CreatureParams(
        id = creatureId,
        pos = pos,
        velocity = Vector2(0, 0),
        destination = Vector2(0, 0),
        lastVelocity = Vector2(0, 0),
        lastPos = pos,
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
        baseVelocity = baseVelocity,
        life = 100f,
        maxLife = 100f,
        attackedCreatureId = None,
        damage = 20f
      )
    )
  }
}
