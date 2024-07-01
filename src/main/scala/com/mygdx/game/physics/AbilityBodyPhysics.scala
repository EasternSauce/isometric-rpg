package com.mygdx.game.physics

import com.mygdx.game.Constants
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityBodyPhysics() {
  private var abilityBodies: Map[EntityId[Ability], AbilityBody] = _
  private var areaWorlds: Map[AreaId, AreaWorld] = _

  def init(areaWorlds: Map[AreaId, AreaWorld]): Unit = {
    abilityBodies = Map()
    this.areaWorlds = areaWorlds
  }

  def update(areaId: AreaId, gameState: GameState): Unit = {
    abilityBodies
      .filter { case (abilityId, _) =>
        gameState.abilities(abilityId).params.currentAreaId == areaId
      }
      .values
      .foreach(_.update(gameState))
  }

  def synchronizeWithGameState(areaId: AreaId, gameState: GameState): Unit = {
    val abilityBodiesToCreate =
      gameState.abilities.keys.toSet -- abilityBodies.keys.toSet
    val abilityBodiesToDestroy =
      abilityBodies.keys.toSet -- gameState.abilities.keys.toSet

    abilityBodiesToCreate
      .filter(abilityId =>
        gameState.abilities(abilityId).params.currentAreaId == areaId
      )
      .foreach(createAbilityBody(_, gameState))
    abilityBodiesToDestroy
      .filter(abilityId =>
        !gameState.abilities.contains(abilityId) || gameState
          .abilities(abilityId)
          .params
          .currentAreaId == areaId
      )
      .foreach(destroyAbilityBody(_, gameState))
  }

  def correctBodyPositions(areaId: AreaId, gameState: GameState): Unit = {
    gameState.abilities.values.foreach(ability =>
      if (
        ability.params.currentAreaId == areaId &&
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
    val ability = gameState.abilities(abilityId)

    val abilityBody = AbilityBody(abilityId)

    val areaWorld = areaWorlds(ability.params.currentAreaId)

    abilityBody.init(areaWorld, ability.pos, gameState)

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
