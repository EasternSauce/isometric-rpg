package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate._
import com.mygdx.game.gamestate.creature.behavior.CreatureBehavior
import com.mygdx.game.gamestate.event._
import com.mygdx.game.input.Input
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{Vector2, WorldDirection}
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
    params: CreatureParams,
    creatureBehavior: CreatureBehavior
) extends Entity {
  def update(
      delta: Float,
      newPos: Vector2,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- creature.updateMovement(
        newPos,
        input,
        clientInformation,
        gameState
      )
      creature <- creature.updateTimers(delta)
      creature <- Outcome.when(creature)(_.deathToBeHandled)(creature =>
        Outcome(creature).withEvents(
          List(
            CreatureDeathEvent(creature.id),
            MakeBodySensorEvent(creature.id)
          )
        )
      )
      creature <- Outcome.when(creature)(creature =>
        creature.params.deathAcknowledged && creature.params.respawnTimer.time > Constants.RespawnTime && !creature.params.respawnDelayInProgress
      )(creature =>
        Outcome(creature).withEvents(
          List(
            CreatureRespawnDelayStartEvent(creature.id),
            TeleportEvent(id, Vector2(5, 5)),
            MakeBodyNonSensorEvent(id)
          )
        )
      )
      creature <- Outcome.when(creature)(creature =>
        creature.params.respawnDelayInProgress && creature.params.respawnDelayTimer.time > Constants.RespawnDelayTime
      )(creature =>
        Outcome(
          creature
            .modify(_.params.respawnDelayInProgress)
            .setTo(false)
        ).withEvents(List(CreatureRespawnEvent(creature.id)))
      )
    } yield creature
  }

  private def deathToBeHandled: Boolean =
    !this.alive && !this.params.deathAcknowledged

  private def updateMovement(
      newPos: Vector2,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- creature.setPos(newPos)
      creature <- Outcome.when(creature)(_.alive)(
        creatureBehavior
          .updateMovement(_, input, clientInformation, gameState)
      )
      creature <- creature.attackTarget()
      creature <- creature.handleWalkingIntoObstacles()
      creature <- creature.updateVelocity()
    } yield creature
  }

  private def attackTarget(): Outcome[Creature] = {
    Outcome.when(this)(_.creatureAttackCompleted) { creature =>
      Outcome(creature.modify(_.params.attackPending).setTo(false))
        .withEvents(
          List(
            CreatureAttackEvent(
              creature.id,
              creature.params.attackedCreatureId.get,
              creature.params.damage
            )
          )
        )
    }
  }

  private[creature] def creatureAttackCompleted: Boolean = {
    this.params.attackedCreatureId.nonEmpty && this.params.attackAnimationTimer.running &&
    this.params.attackAnimationTimer.time > this.params.animationDefinition.attackFrames.totalDuration * 0.8f && this.params.attackPending
  }

  def alive: Boolean = params.life > 0

  private def updateVelocity(): Outcome[Creature] = {
    val vectorTowardsDest = pos.vectorTowards(params.destination)

    val velocity = if (!alive) {
      Vector2(0, 0)
    } else if (vectorTowardsDest.length > 0.2f) {
      vectorTowardsDest.withLength(params.baseSpeed)
    } else {
      vectorTowardsDest.withLength(0f)
    }

    Outcome(
      this
        .modify(_.params.velocity)
        .setTo(velocity)
        .modify(_.params.facingVector)
        .setToIf(velocity.length > 0)(velocity)
    )
  }

  private def handleWalkingIntoObstacles(): Outcome[Creature] = {
    Outcome.when(this)(_.params.lastPosTimer.time > 0.5f)(creature =>
      for {
        creature <- Outcome(
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
        )
        creature <- Outcome.when(creature) { creature =>
          val v1 = creature.params.lastPos
          val v2 = creature.pos

          v1.distance(v2) < 0.2f
        }(_.stopMoving())
        creature <- Outcome(
          creature
            .modify(_.params.lastPos)
            .setTo(this.pos)
        )
      } yield creature
    )
  }

  private[creature] def stopMoving(): Outcome[Creature] = {
    Outcome(
      this
        .modify(_.params.destination)
        .setTo(pos)
    )
  }

  private def setPos(pos: Vector2): Outcome[Creature] = {
    Outcome(this)
      .map(
        _.modify(_.params.pos)
          .setTo(pos)
      )
  }

  def id: EntityId[Creature] = params.id

  private def updateTimers(delta: Float): Outcome[Creature] = {
    Outcome(
      this
        .modify(_.params.animationTimer)
        .using(_.update(delta))
        .modify(_.params.lastPosTimer)
        .using(_.update(delta))
        .modify(_.params.attackAnimationTimer)
        .using(_.update(delta))
        .modify(_.params.deathAnimationTimer)
        .using(_.update(delta))
        .modify(_.params.respawnTimer)
        .using(_.update(delta))
        .modify(_.params.loseAggroTimer)
        .using(_.update(delta))
        .modify(_.params.respawnDelayTimer)
        .using(_.update(delta))
    )
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

  def invisible: Boolean = !params.respawnDelayInProgress

  private[creature] def moveTowardsTarget(
      input: Input,
      mouseWorldPos: Vector2
  ): Outcome[Creature] = {
    if (input.moveButtonPressed) {
      Outcome(
        this
          .modify(_.params.destination)
          .setTo(mouseWorldPos)
          .modify(_.params.attackAnimationTimer)
          .usingIf(params.attackAnimationTimer.running)(_.stop())
      )
    } else {
      Outcome(
        this
          .modify(_.params.destination)
          .setTo(pos)
      )
    }
  }

  def pos: Vector2 = params.pos

  private[creature] def creatureAttackStart(
      otherCreatureId: EntityId[Creature],
      gameState: GameState
  ): Outcome[Creature] = {
    val otherCreature = gameState.creatures(otherCreatureId)

    Outcome.when(this)(_ =>
      otherCreature.pos
        .distance(pos) < params.attackRange
    )(creature =>
      Outcome(
        creature
          .modify(_.params.facingVector)
          .setTo(
            creature.pos
              .vectorTowards(otherCreature.pos)
          )
          .modify(_.params.attackedCreatureId)
          .setTo(Some(otherCreature.id))
          .modify(_.params.attackPending)
          .setTo(true)
      )
    )
  }

  private[creature] def attackAllowed: Boolean = {
    !this.params.attackAnimationTimer.running || this.params.attackAnimationTimer.time >= this.params.animationDefinition.attackFrames.totalDuration + Constants.AttackCooldown
  }

}
