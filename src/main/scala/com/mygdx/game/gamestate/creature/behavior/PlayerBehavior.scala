package com.mygdx.game.gamestate.creature.behavior

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.creature.{Creature, CreaturesFinderUtils}
import com.mygdx.game.gamestate.{GameState, Outcome}
import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.IsometricProjection
import com.softwaremill.quicklens.ModifyPimp

case class PlayerBehavior() extends CreatureBehavior {
  override def updateMovement(
      creature: Creature,
      input: Input,
      clientInformation: ClientInformation,
      gameState: GameState
  ): Outcome[Creature] = {
    val mouseWorldPos: Vector2 = getMouseWorldPos(creature.pos, input)

    for {
      creature <- Outcome.when(creature)(_.alive)(
        _.moveTowardsTarget(input, mouseWorldPos)
      )
      creature <- Outcome.when(creature)(creature =>
        creature.alive && input.attackButtonJustPressed && creature.attackAllowed
      )(performAttack(mouseWorldPos, gameState))
    } yield creature
  }

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
  ): Creature => Outcome[Creature] = { creature =>
    val maybeClosestCreatureId =
      CreaturesFinderUtils.getAliveCreatureIdClosestTo(
        mouseWorldPos,
        List(creature.id),
        gameState
      )

    for {
      creature <- Outcome.when(creature)(_ => maybeClosestCreatureId.nonEmpty)(
        creature =>
          creature.creatureAttackStart(maybeClosestCreatureId.get, gameState)
      )
      creature <- Outcome(
        creature
          .modify(_.params.attackAnimationTimer)
          .using(_.restart())
      )
      creature <- creature.stopMoving()
    } yield creature
  }
}
