package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate._
import com.mygdx.game.gamestate.creature.behavior.CreatureBehavior
import com.mygdx.game.gamestate.event.broadcast.{CreatureSetDestinationEvent, CreatureSetFacingVectorEvent, CreatureShootArrowEvent, MeleeAttackHitsCreatureEvent}
import com.mygdx.game.gamestate.event.gamestate._
import com.mygdx.game.gamestate.event.physics.{MakeBodyNonSensorEvent, MakeBodySensorEvent, TeleportEvent}
import com.mygdx.game.input.Input
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{Vector2, WorldDirection}
import com.mygdx.game.{ClientInformation, Constants}
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
    params: CreatureParams,
    behavior: CreatureBehavior
) extends Entity {
  def update(
      delta: Float,
      newPos: Option[Vector2],
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
      newPos: Option[Vector2],
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- Outcome.when(creature)(_ => newPos.nonEmpty)(
        _.setPos(newPos.get)
      )
      creature <- Outcome.when(creature)(_.alive)(
        behavior.update(_, input, clientInformation, gameState)
      )
      creature <- creature.updateAttacks()
      creature <- creature.handleWalkingIntoObstacle()
      creature <- creature.updateVelocity()
    } yield creature
  }

  private def updateAttacks(): Outcome[Creature] = {
    Outcome.when(this)(_.creatureAttackCompleted) { creature =>
      if (creature.params.primaryWeaponType == PrimaryWeaponType.Bow) {
        Outcome(creature.modify(_.params.attackPending).setTo(false))
          .withEvents(
            List(
              CreatureShootArrowEvent(
                creature.id,
                creature.params.damage
              )
            )
          )
      } else {
        Outcome.when(creature)(_.params.attackedCreatureId.nonEmpty)(creature =>
          Outcome(creature.modify(_.params.attackPending).setTo(false))
            .withEvents(
              List(
                MeleeAttackHitsCreatureEvent(
                  creature.id,
                  creature.params.attackedCreatureId.get,
                  creature.params.damage
                )
              )
            )
        )
      }
    }
  }

  private[creature] def creatureAttackCompleted: Boolean = {
    this.params.attackAnimationTimer.running &&
    this.params.attackAnimationTimer.time > this.params.animationDefinition.attackFrames.totalDuration * 0.8f && this.params.attackPending
  }

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

  def alive: Boolean = params.life > 0

  private def handleWalkingIntoObstacle(): Outcome[Creature] = {
    Outcome.when(this)(
      _.params.lastPosTimer.time > Constants.LastPosSetInterval
    )(creature =>
      for {
        creature <- Outcome(
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
        )
        creature <- Outcome.when(creature) { creature =>
          val v1 = creature.params.lastPos
          val v2 = creature.pos

          v1.distance(v2) < Constants.LastPosMinimumDifference
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
    Outcome(this).withEvents(List(CreatureSetDestinationEvent(id, pos)))
  }

  private def setPos(pos: Vector2): Outcome[Creature] = {
    Outcome(this)
      .map(
        _.modify(_.params.pos)
          .setTo(pos)
      )
  }

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
        .modify(_.params.lastAttackedTimer)
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

  private[creature] def creatureMeleeAttackStart(
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
          .modify(_.params.attackedCreatureId)
          .setTo(Some(otherCreature.id))
          .modify(_.params.attackPending)
          .setTo(true)
      ).withEvents(
        List(
          CreatureSetFacingVectorEvent(
            id,
            creature.pos
              .vectorTowards(otherCreature.pos)
          )
        )
      )
    )
  }

  def id: EntityId[Creature] = params.id

  def pos: Vector2 = params.pos

  private[creature] def creatureRangedAttackStart(
      rangedAttackDir: Vector2
  ): Outcome[Creature] = {
    Outcome[Creature](
      this
        .modify(_.params.attackPending)
        .setTo(true)
    ).withEvents(List(CreatureSetFacingVectorEvent(id, rangedAttackDir)))
  }

  private[creature] def attackAllowed: Boolean = {
    !this.params.attackAnimationTimer.running || this.params.attackAnimationTimer.time >= this.params.animationDefinition.attackFrames.totalDuration + Constants.AttackCooldown
  }
}
