package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mygdx.game.Assets
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityRenderer(abilityId: EntityId[Ability]) extends Renderable {
  var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    textureRegion =
      new TextureRegion(Assets.atlas.findRegion("projectiles"), 0, 0, 64, 64)
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    val pos = IsometricProjection.translateIsoToScreen(ability.pos)

    batch.draw(textureRegion, pos.x - 16, pos.y - 16)
  }

  override def pos(gameState: GameState): Vector2 = {
    val ability = gameState.abilities(abilityId)

    ability.pos
  }

  override def renderPriority(gameState: GameState): Boolean = true
}
