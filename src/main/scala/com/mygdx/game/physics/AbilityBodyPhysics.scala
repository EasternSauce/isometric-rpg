package com.mygdx.game.physics

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityBodyPhysics() {
  private var abilityBodies: Map[EntityId[Ability], AbilityBody] = _
  private var world: World = _

  def init(world: World): Unit = {
    abilityBodies = Map()
    this.world = world
  }

  def update(gameState: GameState): Unit = {
    abilityBodies.values.foreach(_.update(gameState))
  }

  def symchronizeWithGameState(gameState: GameState): Unit = {
    val abilityBodiesToCreate =
      gameState.abilities.keys.toSet -- abilityBodies.keys.toSet
    val abilityBodiesToDestroy =
      abilityBodies.keys.toSet -- gameState.abilities.keys.toSet

    abilityBodiesToCreate.foreach(createAbilityBody(_, gameState))
    abilityBodiesToDestroy.foreach(destroyAbilityBody(_, gameState))
  }

  def correctBodyPositions(gameState: GameState): Unit = {
    gameState.abilities.values.foreach(ability =>
      if (
        abilityBodies.contains(ability.id) && abilityBodies(ability.id).pos
          .distance(ability.pos) > Constants.PhysicalBodyCorrectionDistance
      ) {
        abilityBodies(ability.id).setPos(ability.pos)
      }
    )
  }

  def abilityBodyPositions: Map[EntityId[Ability], Vector2] = {
    abilityBodies.values
      .map(abilityBody => {
        val pos = abilityBody.pos
        (abilityBody.abilityId, pos)
      })
      .toMap
  }

  private def createAbilityBody(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    val creature = gameState.abilities(abilityId)

    val abilityBody = AbilityBody(abilityId)

    abilityBody.init(world, creature.pos, gameState)

    abilityBodies = abilityBodies.updated(abilityId, abilityBody)
  }

  private def destroyAbilityBody(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    if (abilityBodies.contains(abilityId)) {
      abilityBodies(abilityId).onRemove()
      abilityBodies = abilityBodies.removed(abilityId)
    }
  }

}
