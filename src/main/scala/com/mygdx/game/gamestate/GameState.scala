package com.mygdx.game.gamestate

import com.mygdx.game.action.GameStateAction
import com.mygdx.game.gamestate.ability.{Ability, AbilityParams, Arrow}
import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event._
import com.mygdx.game.input.Input
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.mygdx.game.util.Vector2
import com.mygdx.game.{ClientInformation, CoreGame}
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creatures: Map[EntityId[Creature], Creature],
    abilities: Map[EntityId[Ability], Ability],
    creatureCounter: Int = 0,
    abilityCounter: Int = 0,
    creatureSpawnQueue: List[Creature] = List()
) {

  def update(
      creaturePositions: Map[EntityId[Creature], Vector2],
      abilityPositions: Map[EntityId[Ability], Vector2],
      input: Input,
      delta: Float,
      game: CoreGame
  ): GameState = {
    var events: List[Event] = game.gameplay.physics.pollCollisionEvents()
    var actions: List[GameStateAction] = List()

    val newGameState = this
      .modify(_.creatures.each)
      .using { creature =>
        val outcome = creature
          .update(
            delta = delta,
            newPos = creaturePositions.getOrElse(creature.id, creature.pos),
            input = input,
            clientInformation = game.gameplay.clientInformation,
            gameState = this
          )

        events = events.appendedAll(outcome.events)
        actions = actions.appendedAll(outcome.actions)
        outcome.obj
      }
      .modify(_.abilities.each)
      .using { ability =>
        val outcome = ability.update(
          delta = delta,
          newPos = abilityPositions.getOrElse(ability.id, ability.pos),
          gameState = this
        )

        events = events.appendedAll(outcome.events)
        actions = actions.appendedAll(outcome.actions)
        outcome.obj
      }
      .pipe(gameState => {
        val outcome = EnemySpawnUtils.scheduleEnemySpawnActions(gameState)

        events = events.appendedAll(outcome.events)
        actions = actions.appendedAll(outcome.actions)
        outcome.obj
      })
      .pipe(EnemySpawnUtils.processCreatureSpawnQueue)
      .pipe(handleEvents(events))

    game.gameplay.physics.scheduleEvents(events)

    game.onGameStateUpdate(actions)

    actions.foldLeft(newGameState) {
      case (gameState: GameState, action: GameStateAction) =>
        action.applyToGameState(gameState)
    }
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
        case MeleeAttackHitsCreatureEvent(
              sourceCreatureId,
              destinationCreatureId,
              damage
            ) =>
          gameState
            .modify(_.creatures.at(destinationCreatureId))
            .using(creature =>
              creature
                .pipe(registerLastAttackedByCreature(sourceCreatureId))
                .pipe(dealDamageToCreature(damage))
            )

        case CreatureShootArrowEvent(
              sourceCreatureId,
              arrowDirection,
              damage
            ) =>
          val abilityId: EntityId[Ability] =
            EntityId("ability" + gameState.abilityCounter)

          val sourceCreature = gameState.creatures(sourceCreatureId)

          gameState
            .modify(_.abilities)
            .using(
              _.updated(
                abilityId,
                Arrow(
                  AbilityParams(
                    abilityId,
                    sourceCreatureId,
                    sourceCreature.params.pos,
                    arrowDirection,
                    damage
                  )
                )
              )
            )
            .modify(_.abilityCounter)
            .using(_ + 1)

        case AbilityHitsCreatureEvent(abilityId, creatureId) =>
          if (abilities.contains(abilityId) && creatures.contains(creatureId)) {
            val ability = abilities(abilityId)
            val creature = creatures(creatureId)

            if (ability.params.creatureId != creatureId) {
              gameState
                .modify(_.creatures.at(creatureId))
                .using(creature =>
                  creature
                    .pipe(
                      registerLastAttackedByCreature(ability.params.creatureId)
                    )
                    .pipe(dealDamageToCreature(ability.params.damage))
                )
                .modify(_.abilities)
                .usingIf(creature.alive && ability.destroyedOnContact)(
                  _.removed(abilityId)
                )
            } else {
              gameState
            }
          } else {
            gameState
          }

        case AbilityHitsTerrainEvent(abilityId, _) =>
          gameState
            .modify(_.abilities)
            .using(_.removed(abilityId))

        case _ => gameState
      }

    }

  private def registerLastAttackedByCreature(
      sourceCreatureId: EntityId[Creature]
  ): Creature => Creature = { creature =>
    creature
      .modify(_.params.lastAttackedTimer)
      .using(_.restart())
      .modify(_.params.currentTargetId)
      .setTo(Some(sourceCreatureId))
  }

  private def dealDamageToCreature(damage: Float): Creature => Creature = {
    creature =>
      if (creature.params.life - damage > 0) {
        creature
          .modify(_.params.life)
          .setTo(creature.params.life - damage)
      } else {
        creature.modify(_.params.life).setTo(0)
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
      abilities = Map()
    )
  }
}
