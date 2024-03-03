package com.mygdx.game.view

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mygdx.game.Assets
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Vector2

case class AbilityRenderer(abilityId: EntityId[Ability]) extends Renderable {
  var textureRegion: TextureRegion = _

  def init(gameState: GameState): Unit = {
    val ability = gameState.abilities(abilityId)

    textureRegion = new TextureRegion(
      Assets.atlas.findRegion(ability.atlasRegionName),
      ability.atlasRegionX,
      ability.atlasRegionY,
      ability.atlasRegionWidth,
      ability.atlasRegionHeight
    )
  }

  def render(batch: SpriteBatch, gameState: GameState): Unit = {
    if (gameState.abilities.contains(abilityId)) {
      val ability = gameState.abilities(abilityId)

      val pos = IsometricProjection.translatePosIsoToScreen(ability.pos)

      val angle = IsometricProjection
        .translatePosIsoToScreen(ability.params.facingVector)
        .angleDeg

      batch.draw(
        textureRegion,
        pos.x - ability.atlasRegionWidth / 2f,
        pos.y - ability.atlasRegionHeight / 2f,
        ability.worldWidth,
        ability.worldHeight,
        angle
      )
    }
  }

  override def pos(gameState: GameState): Vector2 = {
    val ability = gameState.abilities(abilityId)

    ability.pos
  }

  override def renderPriority(gameState: GameState): Boolean = true
}
