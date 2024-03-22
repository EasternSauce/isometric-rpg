package com.mygdx.game.gamestate.creature

import com.mygdx.game.Constants
import com.mygdx.game.gamestate._
import com.mygdx.game.gamestate.creature.behavior.EnemyBehavior
import com.mygdx.game.gamestate.event.broadcast.{CreatureShootArrowEvent, MeleeAttackHitsCreatureEvent}
import com.mygdx.game.gamestate.event.gamestate._
import com.mygdx.game.gamestate.event.physics.{MakeBodyNonSensorEvent, MakeBodySensorEvent, TeleportEvent}
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{Vector2, WorldDirection}
import com.softwaremill.quicklens.ModifyPimp

case class Creature(
    params: CreatureParams
) extends Entity {
  private val enemyBehavior = EnemyBehavior()

  def update(
      delta: Float,
      newPos: Option[Vector2],
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- creature.updateMovement(
        newPos,
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
    !alive && !params.deathAcknowledged

  private def updateMovement(
      newPos: Option[Vector2],
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- Outcome.when(creature)(_ => newPos.nonEmpty)(
        _.setPos(newPos.get)
      )
      creature <- creature.updateVelocity()
      creature <- Outcome.when(creature)(creature =>
        !creature.params.player && creature.alive
      )(
        enemyBehavior.update(_, gameState)
      )
      creature <- creature.updateAttacks()
      creature <- creature.handleWalkingIntoObstacle()
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
    params.attackAnimationTimer.running &&
    params.attackAnimationTimer.time > params.animationDefinition.attackFrames.totalDuration * 0.8f &&
    params.attackPending
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
        }(creature =>
          Outcome(creature.modify(_.params.destination).setTo(creature.pos))
        )
        creature <- Outcome(
          creature
            .modify(_.params.lastPos)
            .setTo(this.pos)
        )
      } yield creature
    )
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
    WorldDirection.fromVector(params.facingVector)
  }

  def moving: Boolean = params.velocity.length > 0

  def invisible: Boolean = !params.respawnDelayInProgress

  def id: EntityId[Creature] = params.id

  def pos: Vector2 = params.pos

  def attackingAllowed: Boolean = {
    !this.params.attackAnimationTimer.running || this.params.attackAnimationTimer.time >= this.params.animationDefinition.attackFrames.totalDuration + Constants.AttackCooldown
  }
}
