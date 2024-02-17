package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.creature.{Creature, CreatureAttackUtils, CreatureFactory}
import com.mygdx.game.gamestate.event.{CreatureDeathEvent, CreatureRespawnEvent, Event}
import com.mygdx.game.input.Input
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creatures: Map[EntityId[Creature], Creature],
    creatureCounter: Int
) {

  def update(
      creaturePositions: Map[EntityId[Creature], Vector2],
      input: Input,
      clientInformation: ClientInformation,
      physics: Physics,
      delta: Float
  ): GameState = {
    var events: List[Event] = List()
    val newGameState = this
      .modify(_.creatures.each)
      .using { creature =>
        val outcome = creature
          .update(
            delta = delta,
            newPos = creaturePositions(creature.id),
            input = input,
            clientInformation = clientInformation,
            gameState = this
          )

        events = events.appendedAll(outcome.events)
        outcome.obj
      }
      .pipe(EnemySpawnUtils.processSpawns)
      .pipe(CreatureAttackUtils.processAttacks)
      .pipe(handleEvents(events))

    physics.scheduleEvents(events)

    newGameState
  }

  def handleEvents(events: List[Event]): GameState => GameState = gameState =>
    events.foldLeft(gameState) { case (gameState: GameState, event: Event) =>
      event match {
        case CreatureDeathEvent(creatureId) =>
          gameState
            .modify(_.creatures.at(creatureId))
            .using(creature =>
              creature
                .modify(_.params.deathAcknowledged)
                .setTo(true)
                .modify(_.params.deathAnimationTimer)
                .using(_.restart())
                .modify(_.params.attackAnimationTimer)
                .using(_.restart().stop())
                .pipeIf(_.params.player)(
                  _.modify(_.params.respawnTimer)
                    .using(_.restart())
                )
            )
        case CreatureRespawnEvent(creatureId) =>
          gameState
            .modify(_.creatures.at(creatureId))
            .using(creature =>
              creature
                .modify(_.params.life)
                .setTo(creature.params.maxLife)
                .modify(_.params.deathAcknowledged)
                .setTo(false)
                .modify(_.params.respawnTimer)
                .using(_.stop())
            )
        case _ => gameState
      }

    }
}

object GameState {
  def initialState(clientInformation: ClientInformation): GameState = {
    val player =
      CreatureFactory.male1(
        clientInformation.clientCreatureId,
        Vector2(5f, 5f),
        player = true,
        baseSpeed = 4f
      )

    GameState(
      creatures = Map(
        clientInformation.clientCreatureId ->
          player
      ),
      creatureCounter = 0
    )
  }
}
