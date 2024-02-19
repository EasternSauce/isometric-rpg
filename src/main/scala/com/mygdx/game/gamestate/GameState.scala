package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.mygdx.game.gamestate.ability.{Ability, AbilityParams, Arrow}
import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event._
import com.mygdx.game.input.Input
import com.mygdx.game.physics.Physics
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creatures: Map[EntityId[Creature], Creature],
    abilities: Map[EntityId[Ability], Ability],
    creatureCounter: Int = 0
) {

  def update(
      creaturePositions: Map[EntityId[Creature], Vector2],
      abilityPositions: Map[EntityId[Ability], Vector2],
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
      .modify(_.abilities.each)
      .using { ability =>
        val outcome = ability.update(
          delta = delta,
          newPos = abilityPositions(ability.id),
          gameState = this
        )

        events = events.appendedAll(outcome.events)
        outcome.obj
      }
      .pipe(EnemySpawnUtils.processSpawns)
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
                .using(_.stop())
                .pipeIf(_.params.player)(
                  _.modify(_.params.respawnTimer)
                    .using(_.restart())
                )
            )
        case CreatureRespawnDelayStartEvent(creatureId) =>
          gameState
            .modify(_.creatures.at(creatureId))
            .using(
              _.modify(_.params.respawnDelayTimer)
                .using(_.restart())
                .modify(_.params.respawnDelayInProgress)
                .setTo(true)
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
        case CreatureAttackEvent(
              _,
              destinationCreatureId,
              damage
            ) =>
          gameState
            .modify(_.creatures.at(destinationCreatureId))
            .using(creature =>
              if (creature.params.life - damage > 0) {
                creature
                  .modify(_.params.life)
                  .setTo(creature.params.life - damage)
              } else {
                creature.modify(_.params.life).setTo(0)
              }
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
      abilities = Map(
        EntityId[Ability]("testability1") -> Arrow(
          AbilityParams(
            EntityId[Ability]("testability1"),
            pos = Vector2(3, 3),
            velocity = Vector2(0f, 2f)
          )
        )
      )
    )
  }
}
