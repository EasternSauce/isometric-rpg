package com.mygdx.game.gamestate

import com.mygdx.game.core.CoreGame
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.{Creature, CreatureFactory}
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.util.Vector2
import com.softwaremill.quicklens.ModifyPimp

case class GameState(
    creatures: Map[EntityId[Creature], Creature],
    abilities: Map[EntityId[Ability], Ability],
    creatureCounter: Int = 0,
    abilityCounter: Int = 0
) {

  def update(
      delta: Float,
      game: CoreGame
  ): GameState = {
    val gameStateOutcome = for {
      gameState <- Outcome(this)
      gameState <- gameState.creatures.keySet.foldLeft(Outcome(gameState)) {
        case (gameStateOutcome, creatureId) =>
          gameStateOutcome.flatMap(_.updateCreature(creatureId, delta, game))
      }
      gameState <- gameState.abilities.keySet.foldLeft(Outcome(gameState)) {
        case (gameStateOutcome, abilityId) =>
          gameStateOutcome.flatMap(_.updateAbility(abilityId, delta, game))
      }
      gameState <- gameState.updateEnemySpawns()
      gameState <- gameState.handleCreatePlayerCreatures(
        game.gameplay.scheduledPlayerCreaturesToCreate
      )
    } yield gameState

    game.gameplay.clearScheduledPlayerCreaturesToCreate()

    game.applyOutcomeEvents(gameStateOutcome)
  }

  def handleCreatePlayerCreatures(
      scheduledPlayerCreaturesToCreate: List[String]
  ): Outcome[GameState] =
    Outcome(
      scheduledPlayerCreaturesToCreate.foldLeft(this) { case (gameState, id) =>
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
    )

  private def updateEnemySpawns(): Outcome[GameState] =
    EnemySpawnUtils.handleEnemySpawns(this)

  private def updateCreature(
      creatureId: EntityId[Creature],
      delta: Float,
      game: CoreGame
  ): Outcome[GameState] = {
    val creature: Creature = creatures(creatureId)

    val creatureOutcome = creature
      .update(
        delta = delta,
        newPos = game.gameplay.physics.creatureBodyPositions.get(creature.id),
        gameState = this
      )

    creatureOutcome.map(updatedCreature =>
      this.modify(_.creatures).using(_.updated(creatureId, updatedCreature))
    )
  }

  private def updateAbility(
      abilityId: EntityId[Ability],
      delta: Float,
      game: CoreGame
  ): Outcome[GameState] = {
    val ability: Ability = abilities(abilityId)

    val abilityOutcome = ability.update(
      delta = delta,
      newPos = game.gameplay.physics.abilityBodyPositions.get(ability.id),
      gameState = this
    )

    abilityOutcome.map(updatedAbility =>
      this.modify(_.abilities).using(_.updated(abilityId, updatedAbility))
    )
  }

  def handleGameStateEvents(
      events: List[GameStateEvent]
  ): GameState =
    events.foldLeft(this) {
      case (gameState: GameState, event: GameStateEvent) =>
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
