package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mygdx.game.Assets
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2
import com.mygdx.game.util.WorldDirection.WorldDirection

case class AbilityRenderer(abilityId: EntityId[Ability]) extends Renderable {
  var textureRegions: Map[WorldDirection, TextureRegion] = _

  def init(gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    textureRegions = ability.worldDirectionRegionMapping.map {
      case (worldDirection, (x, y)) =>
        (
          worldDirection,
          new TextureRegion(
            Assets.atlas.findRegion(ability.atlasRegionName),
            x,
            y,
            ability.atlasRegionWidth,
            ability.atlasRegionHeight
          )
        )
    }
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    val pos = IsometricProjection.translateIsoToScreen(ability.pos)

    batch.draw(textureRegions(ability.facingDirection), pos.x - 16, pos.y - 16)
  }

  override def pos(gameState: GameState): Vector2 = {
    val ability = gameState.abilities(abilityId)

    ability.pos
  }

  override def renderPriority(gameState: GameState): Boolean = true
}
