package com.mygdx.game.gamestate

import com.mygdx.game.ClientInformation
import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent
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
      .pipe(handleEvents(sideEffectsCollector.gameStateEvents))

    game.gameplay.physics.scheduleEvents(sideEffectsCollector.physicsEvents)

    game.onGameStateUpdate(sideEffectsCollector.broadcastEvents)

    applyBroadcastEventsToGameState(
      sideEffectsCollector.broadcastEvents,
      newGameState
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
        clientInformation = game.gameplay.clientInformation,
        gameState = this
      )

    outcome.withSideEffectsExtracted(sideEffectsCollector)
  }

  private def applyBroadcastEventsToGameState(
      broadcastEvents: List[BroadcastEvent],
      gameState: GameState
  ) = {
    broadcastEvents.foldLeft(gameState) {
      case (gameState: GameState, broadcastEvent: BroadcastEvent) =>
        broadcastEvent.applyToGameState(gameState)
    }
  }

  def handleEvents(events: List[GameStateEvent]): GameState => GameState =
    gameState =>
      events.foldLeft(gameState) {
        case (gameState: GameState, event: GameStateEvent) =>
          event.applyToGameState(gameState)
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
