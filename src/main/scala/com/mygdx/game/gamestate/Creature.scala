package com.mygdx.game.gamestate

import com.mygdx.game.input.Input
import com.mygdx.game.util.Chaining.customUtilChainingOps
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
      delta: Float,
      newPos: Vector2,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    this
      .updateMovement(newPos, input, clientInformation, gameState)
      .updateTimers(delta)
      .pipeIf(_.deathToBeHandled)(_.onDeath())
  }

  private def onDeath(): Creature = {
    this
      .modify(_.params.deathRegistered)
      .setTo(true)
      .modify(_.params.deathAnimationTimer)
      .using(_.restart())
      .modify(_.params.attackAnimationTimer)
      .using(_.restart().stop())
  }

  private def deathToBeHandled: Boolean =
    !this.alive && !this.params.deathRegistered

  def alive: Boolean = params.life > 0

  private def updateMovement(
      newPos: Vector2,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    this
      .setPos(newPos)
      .pipe(creature => {
        if (creature.params.player) {
          creature.updatePlayerMovement(input, gameState)
        } else {
          creature.updateEnemyMovement(clientInformation, gameState)
        }
      })
      .stopMovingIfStuck()
      .updateVelocity()
  }

  private def updateVelocity(): Creature = {
    val vectorTowardsDest = params.pos.vectorTowards(params.destination)

    val velocity = if (!alive) {
      Vector2(0, 0)
    } else if (vectorTowardsDest.length > 0.2f) {
      vectorTowardsDest.withLength(params.baseVelocity)
    } else {
      vectorTowardsDest.withLength(0f)
    }

    this
      .modify(_.params.velocity)
      .setTo(velocity)
      .modify(_.params.facingVector)
      .setToIf(velocity.length > 0)(velocity)
  }

  private def stopMovingIfStuck(): Creature = {
    this
      .pipeIf(_.params.lastPosTimer.time > 0.5f)(
        _.modify(_.params.lastPosTimer)
          .using(_.restart())
          .pipeIf { creature =>
            val v1 = creature.params.lastPos
            val v2 = creature.params.pos

            v1.distance(v2) < 0.2f
          }(_.stopMoving())
          .modify(_.params.lastPos)
          .setTo(this.params.pos)
      )
  }

  private def updateEnemyMovement(
      clientInformation: ClientInformation,
      gameState: GameState
  ): Creature = {
    enemyPursuePlayer(clientInformation, gameState)
  }

  private def enemyPursuePlayer(
      clientInformation: ClientInformation,
      gameState: GameState
  ) = {
    val player = gameState.creatures(clientInformation.clientCreatureId)

    this
      .pipe { creature =>
        val distanceToPlayer = params.pos.distance(player.params.pos)

        if (distanceToPlayer > Constants.EnemyAttackDistance) {
          creature
            .modify(_.params.destination)
            .setTo(player.params.pos)
        } else {
          creature
            .pipeIf(creature => player.alive && creature.attackingAllowed)(
              _.modify(_.params.attackAnimationTimer)
                .using(_.restart())
                .attack(player)
            )
            .stopMoving()
        }
      }
  }

  private def updatePlayerMovement(
      input: Input,
      gameState: GameState
  ): Creature = {
    val mouseWorldPos: Vector2 = getMouseWorldPos(this.params.pos, input)

    this
      .pipeIf(_.alive)(_.moveTowardsTarget(input, mouseWorldPos))
      .pipeIf(_.isPlayerAttacking(input))(
        _.performAttack(mouseWorldPos, gameState)
      )
  }

  private def moveTowardsTarget(
      input: Input,
      mouseWorldPos: Vector2
  ): Creature = {
    if (isPlayerMoving(input)) {
      this
        .modify(_.params.destination)
        .setTo(mouseWorldPos)
        .modify(_.params.attackAnimationTimer)
        .usingIf(params.attackAnimationTimer.isRunning)(_.stop())
    } else {
      this
        .modify(_.params.destination)
        .setTo(params.pos)
    }
  }

  private def isPlayerMoving(input: Input): Boolean = input.moveButtonPressed

  private def getMouseWorldPos(playerPos: Vector2, input: Input): Vector2 = {
    val mousePos = input.mousePos

    val mouseScreenPos =
      IsometricProjection.translateScreenToIso(mousePos)

    val mouseWorldPos = playerPos.add(mouseScreenPos)
    mouseWorldPos
  }

  private def performAttack(
      mouseWorldPos: Vector2,
      gameState: GameState
  ): Creature = {
    val closestCreature =
      CreaturesFinderUtils.getAliveCreatureClosestTo(
        mouseWorldPos,
        List(params.id),
        gameState
      )

    (closestCreature match {
      case Some(closestCreature) =>
        this
          .pipeIf(_ =>
            closestCreature.params.pos
              .distance(params.pos) < Constants.AttackRange
          )(creature =>
            creature
              .modify(_.params.facingVector)
              .setTo(
                creature.params.pos
                  .vectorTowards(closestCreature.params.pos)
              )
              .attack(closestCreature)
          )
      case None => this
    })
      .modify(_.params.attackAnimationTimer)
      .using(_.restart())
      .stopMoving()
  }

  private def attack(otherCreature: Creature): Creature = {
    this
      .modify(_.params.attackedCreatureId)
      .setTo(Some(otherCreature.params.id))
  }

  private def stopMoving(): Creature = {
    this
      .modify(_.params.destination)
      .setTo(params.pos)
  }

  private def isPlayerAttacking(input: Input): Boolean =
    this.alive && input.attackButtonJustPressed && !input.moveButtonPressed && this.attackingAllowed

  private def attackingAllowed: Boolean =
    !this.params.attackAnimationTimer.isRunning || this.params.attackAnimationTimer.time >= Constants.AttackAnimationDuration + Constants.AttackCooldown

  private def setPos(pos: Vector2): Creature = {
    this
      .modify(_.params.pos)
      .setTo(pos)
  }

  private def updateTimers(delta: Float): Creature = {
    this
      .modify(_.params.animationTimer)
      .using(_.update(delta))
      .modify(_.params.lastPosTimer)
      .using(_.update(delta))
      .modify(_.params.attackAnimationTimer)
      .using(_.update(delta))
      .modify(_.params.deathAnimationTimer)
      .using(_.update(delta))
  }

  def facingDirection: WorldDirection = {
    val angleDeg = params.facingVector.angleDeg

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
        facingVector = Vector2(0, 0),
        lastPos = pos,
        textureNames = Map(
          CreatureAnimationType.Body -> "steel_armor",
          CreatureAnimationType.Head -> "male_head1",
          CreatureAnimationType.Weapon -> "greatstaff",
          CreatureAnimationType.Bow -> "shield"
        ),
        animationTimer = SimpleTimer(isRunning = true),
        lastPosTimer = SimpleTimer(isRunning = true),
        attackAnimationTimer = SimpleTimer(isRunning = false),
        player = player,
        baseVelocity = baseVelocity,
        life = 100f,
        maxLife = 100f,
        attackedCreatureId = None,
        damage = 20f,
        deathRegistered = false,
        deathAnimationTimer = SimpleTimer(isRunning = false)
      )
    )
  }
}
