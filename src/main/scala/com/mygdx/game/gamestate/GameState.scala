package com.mygdx.game.gamestate

import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
import com.mygdx.game.gamestate.event.collision.CollisionEvent
import com.mygdx.game.gamestate.event.gamestate.GameStateEvent
import com.mygdx.game.input.Input
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

import scala.util.chaining.scalaUtilChainingOps

case class GameState(
    creatures: Map[EntityId[Creature], Creature],
    abilities: Map[EntityId[Ability], Ability],
    creatureCounter: Int = 0,
    abilityCounter: Int = 0
) {

  def update(
      input: Input,
      delta: Float,
      game: CoreGame
  ): GameState = {
    val sideEffectsCollector = GameStateSideEffectsCollector(
      events = game.gameplay.physics.pollCollisionEvents()
    )

    val newGameState = this
      .modify(_.creatures.each)
      .using(updateCreature(input, delta, sideEffectsCollector, game))
      .modify(_.abilities.each)
      .using(updateAbility(delta, sideEffectsCollector, game))
      .pipe(updateEnemySpawns(sideEffectsCollector))
      .pipe(
        handleCreatePlayerCreatures(
          game.gameplay.scheduledPlayerCreaturesToCreate
        )
      )

    game.gameplay.clearScheduledPlayerCreaturesToCreate()

    game.gameplay.physics.scheduleEvents(sideEffectsCollector.physicsEvents)

    game.applySideEffectsToGameState(newGameState, sideEffectsCollector)
  }

  def handleCreatePlayerCreatures(
      scheduledPlayerCreaturesToCreate: List[String]
  ): GameState => GameState = gameState =>
    scheduledPlayerCreaturesToCreate.foldLeft(gameState) {
      case (gameState, id) =>
        val creatureId = EntityId[Creature](id)
        gameState
          .modify(_.creatures)
          .using(
            _.updated(
              creatureId,
              CreatureFactory
                .male1(
                  creatureId,
                  Vector2(
                    3f + 3f * Math.random().toFloat,
                    3f + 3f * Math.random().toFloat
                  ),
                  player = true,
                  4f
                )
            )
          )
    }

  private def updateEnemySpawns(
      sideEffectsCollector: GameStateSideEffectsCollector
  ): GameState => GameState = { gameState =>
    {
      val outcome = EnemySpawnUtils.handleEnemySpawns(gameState)

      outcome.withSideEffectsExtracted(sideEffectsCollector)
    }
  }

  private def updateAbility(
      delta: Float,
      sideEffectsCollector: GameStateSideEffectsCollector,
      game: CoreGame
  ): Ability => Ability = { ability =>
    val outcome = ability.update(
      delta = delta,
      newPos = game.gameplay.physics.abilityBodyPositions.get(ability.id),
      gameState = this
    )

    outcome.withSideEffectsExtracted(sideEffectsCollector)
  }

  private def updateCreature(
      input: Input,
      delta: Float,
      sideEffectsCollector: GameStateSideEffectsCollector,
      game: CoreGame
  ): Creature => Creature = { creature =>
    val outcome = creature
      .update(
        delta = delta,
        newPos = game.gameplay.physics.creatureBodyPositions.get(creature.id),
        input = input,
        gameState = this
      )

    outcome.withSideEffectsExtracted(sideEffectsCollector)
  }

  def handleGameStateEvents(
      events: List[GameStateEvent]
  ): GameState =
    events.foldLeft(this) {
      case (gameState: GameState, event: GameStateEvent) =>
        event.applyToGameState(gameState)
    }

  def handleCollisionEvents(
      events: List[CollisionEvent]
  ): GameState =
    events.foldLeft(this) {
      case (gameState: GameState, event: CollisionEvent) =>
        event.applyToGameState(gameState)
    }

  def handleBroadcastEvents(
      events: List[BroadcastEvent]
  ): GameState =
    events.foldLeft(this) {
      case (gameState: GameState, event: BroadcastEvent) =>
        event.applyToGameState(gameState)
    }
}

object GameState {
  def initialState(): GameState = {
//    val player =
//      CreatureFactory.male1(
//        clientInformation.clientCreatureId,
//        Vector2(5f, 5f),
//        player = true,
//        baseSpeed = 4f
//      )

    GameState(
      creatures = Map(
//        clientInformation.clientCreatureId ->
//          player
      ),
      abilities = Map()
    )
  }
}
