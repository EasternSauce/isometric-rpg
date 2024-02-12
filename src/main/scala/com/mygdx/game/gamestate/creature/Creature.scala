package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate._
import com.mygdx.game.gamestate.creature.behavior.{CreatureBehavior, EnemyBehavior, PlayerBehavior}
import com.mygdx.game.gamestate.event.{MakeBodyNonSensorEvent, MakeBodySensorEvent, TeleportEvent}
import com.mygdx.game.input.Input
import com.mygdx.game.util.WorldDirection.WorldDirection
import com.mygdx.game.util.{SimpleTimer, Vector2, WorldDirection}
import com.mygdx.game.view.CreatureAnimationType
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
      creature <- Outcome.when(creature)(_.deathToBeHandled)(_.onDeath())
      creature <- Outcome.when(creature)(creature =>
        creature.params.deathAcknowledged && creature.params.respawnTimer.time > Constants.RespawnTime
      )(_.respawn())
    } yield creature
  }

  private def respawn(): Outcome[Creature] = {
    Outcome(this)
      .map(
        _.modify(_.params.life)
          .setTo(this.params.maxLife)
          .modify(_.params.deathAcknowledged)
          .setTo(false)
          .modify(_.params.respawnTimer)
          .using(_.stop())
      )
      .withEvents(
        List(TeleportEvent(id, Vector2(5, 5)), MakeBodyNonSensorEvent(id))
      )
  }

  private def onDeath(): Outcome[Creature] = {
    for {
      creature <- Outcome(this)
      creature <- Outcome(
        creature
          .modify(_.params.deathAcknowledged)
          .setTo(true)
          .modify(_.params.deathAnimationTimer)
          .using(_.restart())
          .modify(_.params.attackAnimationTimer)
          .using(_.restart().stop())
      ).withEvents(List(MakeBodySensorEvent(creature.id)))
      creature <- Outcome.when(creature)(_.params.player)(creature =>
        Outcome(
          creature
            .modify(_.params.respawnTimer)
            .using(_.restart())
        )
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
      creature <- creature.stopMovingIfStuck()
      creature <- creature.updateVelocity()
    } yield creature
  }

  def alive: Boolean = params.life > 0

  def id: EntityId[Creature] = params.id

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

  private def stopMovingIfStuck(): Outcome[Creature] = {
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

  def takeDamage(damage: Float): Creature = {
    if (params.life - damage > 0) {
      this.modify(_.params.life).setTo(params.life - damage)
    } else {
      this.modify(_.params.life).setTo(0)
    }
  }

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
          .usingIf(params.attackAnimationTimer.isRunning)(_.stop())
      )
    } else {
      Outcome(
        this
          .modify(_.params.destination)
          .setTo(pos)
      )
    }
  }

  private[creature] def performAttack(
      mouseWorldPos: Vector2,
      gameState: GameState
  ): Outcome[Creature] = {
    val maybeClosestCreature =
      CreaturesFinderUtils.getAliveCreatureClosestTo(
        mouseWorldPos,
        List(id),
        gameState
      )

    for {
      creature <- attackCreature(maybeClosestCreature)
      creature <- Outcome(
        creature
          .modify(_.params.attackAnimationTimer)
          .using(_.restart())
      )
      creature <- creature.stopMoving()
    } yield creature
  }

  private def attackCreature(
      maybeClosestCreature: Option[Creature]
  ): Outcome[Creature] = maybeClosestCreature match {
    case Some(closestCreature) =>
      Outcome.when(this)(_ =>
        closestCreature.pos
          .distance(pos) < params.attackRange
      )(creature =>
        Outcome(
          creature
            .modify(_.params.facingVector)
            .setTo(
              creature.pos
                .vectorTowards(closestCreature.pos)
            )
            .modify(_.params.attackedCreatureId)
            .setTo(Some(closestCreature.id))
        )
      )
    case None => Outcome(this)
  }

  private[creature] def stopMoving(): Outcome[Creature] = {
    Outcome(
      this
        .modify(_.params.destination)
        .setTo(pos)
    )
  }

  private[creature] def attackingAllowed: Boolean =
    !this.params.attackAnimationTimer.isRunning || this.params.attackAnimationTimer.time >= this.params.animationDefinition.attackFrames.totalDuration + Constants.AttackCooldown

  def pos: Vector2 = params.pos
}

object Creature {
  def male1(
      creatureId: EntityId[Creature],
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    Creature(
      creature.CreatureParams(
        id = creatureId,
        pos = pos,
        velocity = Vector2(0, 0),
        destination = pos,
        facingVector = Vector2(0, 0),
        lastPos = pos,
        textureNames = Map(
          CreatureAnimationType.Body -> "steel_armor",
          CreatureAnimationType.Head -> "male_head1",
          CreatureAnimationType.Weapon -> "greatstaff",
          CreatureAnimationType.Shield -> "shield"
        ),
        animationTimer = SimpleTimer(isRunning = true),
        lastPosTimer = SimpleTimer(isRunning = true),
        attackAnimationTimer = SimpleTimer(isRunning = false),
        player = player,
        baseSpeed = baseSpeed,
        life = 100f,
        maxLife = 100f,
        attackedCreatureId = None,
        damage = 20f,
        deathAcknowledged = false,
        deathAnimationTimer = SimpleTimer(isRunning = false),
        animationDefinition = Constants.HumanAnimationDefinition,
        attackRange = 1f,
        respawnTimer = SimpleTimer(isRunning = false)
      ),
      creatureBehavior = if (player) PlayerBehavior() else EnemyBehavior()
    )
  }

  def rat(
      creatureId: EntityId[Creature],
      pos: Vector2,
      player: Boolean,
      baseSpeed: Float
  ): Creature = {
    Creature(
      creature.CreatureParams(
        id = creatureId,
        pos = pos,
        velocity = Vector2(0, 0),
        destination = pos,
        facingVector = Vector2(0, 0),
        lastPos = pos,
        textureNames = Map(
          CreatureAnimationType.Body -> "rat"
        ),
        animationTimer = SimpleTimer(isRunning = true),
        lastPosTimer = SimpleTimer(isRunning = true),
        attackAnimationTimer = SimpleTimer(isRunning = false),
        player = player,
        baseSpeed = baseSpeed,
        life = 40f,
        maxLife = 40f,
        attackedCreatureId = None,
        damage = 100f,
        deathAcknowledged = false,
        deathAnimationTimer = SimpleTimer(isRunning = false),
        animationDefinition = Constants.RatAnimationDefinition,
        attackRange = 0.8f,
        respawnTimer = SimpleTimer(isRunning = false)
      ),
      creatureBehavior = if (player) PlayerBehavior() else EnemyBehavior()
    )
  }
}
