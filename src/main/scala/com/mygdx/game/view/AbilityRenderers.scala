package com.mygdx.game.view

import com.mygdx.game.SpriteBatches
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.area.AreaId
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityRenderers() {

  private var abilityRenderers: Map[EntityId[Ability], AbilityRenderer] = _

  def init(gameState: GameState): Unit = {
    abilityRenderers = Map()
  }

  def renderAbilities(
      spriteBatches: SpriteBatches,
      worldCameraPos: Vector2,
      currentAreaId: Option[AreaId],
      gameState: GameState
  ): Unit = {
    abilityRenderers.values
      .filter(abilityRenderer =>
        currentAreaId.contains(abilityRenderer.areaId(gameState))
      )
      .foreach(
        _.render(spriteBatches.worldSpriteBatch, worldCameraPos, gameState)
      )
  }

  def update(gameState: GameState): Unit = {
    val abilityRenderersToCreate =
      gameState.abilities.keys.toSet -- abilityRenderers.keys.toSet
    val abilityRenderersToDestroy =
      abilityRenderers.keys.toSet -- gameState.abilities.keys.toSet

    abilityRenderersToCreate.foreach(createAbilityRenderer(_, gameState))
    abilityRenderersToDestroy.foreach(destroyAbilityRenderer(_, gameState))
  }

  private def createAbilityRenderer(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    val abilityRenderer = AbilityRenderer(abilityId)
    abilityRenderer.init(gameState)
    abilityRenderers = abilityRenderers.updated(abilityId, abilityRenderer)
  }

  private def destroyAbilityRenderer(
      abilityId: EntityId[Ability],
      gameState: GameState
  ): Unit = {
    abilityRenderers = abilityRenderers.removed(abilityId)
  }

}
