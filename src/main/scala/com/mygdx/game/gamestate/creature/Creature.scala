package com.mygdx.game.gamestate.creature

import com.mygdx.game.gamestate._
import com.mygdx.game.gamestate.creature.behavior.{CreatureBehavior, EnemyBehavior, PlayerBehavior}
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
      a <- Outcome(this)
      b <- Outcome(
        a
          .modify(_.params.teleportPos)
          .setTo(None)
      )
      c <- b.updateMovement(newPos, input, clientInformation, gameState)
      d <- c.updateTimers(delta)
      e <- Outcome.when(d)(_.deathToBeHandled)(_.onDeath())
    } yield e
  }

  private def onDeath(): Outcome[Creature] = {
    Outcome(
      this
        .modify(_.params.deathRegistered)
        .setTo(true)
        .modify(_.params.deathAnimationTimer)
        .using(_.restart())
        .modify(_.params.attackAnimationTimer)
        .using(_.restart().stop())
        // TODO: do all this after few seconds delay... and only do this for player
        .modify(_.params.teleportPos)
        .setTo(Some(Vector2(5, 5)))
        .modify(_.params.life)
        .setTo(100)
        .modify(_.params.deathRegistered)
        .setTo(false)
    )
  }

  private def deathToBeHandled: Boolean =
    !this.alive && !this.params.deathRegistered

  def alive: Boolean = params.life > 0

  private def updateMovement(
      newPos: Vector2,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    for {
      a <- Outcome(this)
      b <- a.setPos(newPos)
      c <- Outcome.when(b)(_.alive)(
        creatureBehavior
          .updateMovement(_, input, clientInformation, gameState)
      )
      d <- c.stopMovingIfStuck()
      e <- d.updateVelocity()
    } yield e
  }

  private def updateVelocity(): Outcome[Creature] = {
    val vectorTowardsDest = params.pos.vectorTowards(params.destination)

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
        a <- Outcome(
          creature
            .modify(_.params.lastPosTimer)
            .using(_.restart())
        )
        b <- Outcome.when(a) { creature =>
          val v1 = creature.params.lastPos
          val v2 = creature.params.pos

          v1.distance(v2) < 0.2f
        }(_.stopMoving())
        c <- Outcome(
          b.modify(_.params.lastPos)
            .setTo(this.params.pos)
        )
      } yield c
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
          .setTo(params.pos)
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
        List(params.id),
        gameState
      )

    for {
      a <- attackCreature(maybeClosestCreature)
      b <- Outcome(
        a.modify(_.params.attackAnimationTimer)
          .using(_.restart())
      )
      c <- b.stopMoving()
    } yield c
  }

  private def attackCreature(
      maybeClosestCreature: Option[Creature]
  ): Outcome[Creature] = maybeClosestCreature match {
      case Some(closestCreature) =>
        Outcome.when(this)(_ =>
          closestCreature.params.pos
            .distance(params.pos) < params.attackRange
        )(creature =>
          Outcome(
            creature
              .modify(_.params.facingVector)
              .setTo(
                creature.params.pos
                  .vectorTowards(closestCreature.params.pos)
              )
              .modify(_.params.attackedCreatureId)
              .setTo(Some(closestCreature.params.id))
          )
        )
      case None => Outcome(this)
    }

  private[creature] def stopMoving(): Outcome[Creature] = {
    Outcome(
      this
        .modify(_.params.destination)
        .setTo(params.pos)
    )
  }

  private[creature] def attackingAllowed: Boolean =
    !this.params.attackAnimationTimer.isRunning || this.params.attackAnimationTimer.time >= this.params.animationDefinition.attackFrames.totalDuration + Constants.AttackCooldown
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
        deathRegistered = false,
        deathAnimationTimer = SimpleTimer(isRunning = false),
        animationDefinition = Constants.HumanAnimationDefinition,
        attackRange = 1f,
        teleportPos = None
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
        damage = 5f,
        deathRegistered = false,
        deathAnimationTimer = SimpleTimer(isRunning = false),
        animationDefinition = Constants.RatAnimationDefinition,
        attackRange = 0.8f,
        teleportPos = None
      ),
      creatureBehavior = if (player) PlayerBehavior() else EnemyBehavior()
    )
  }
}
