package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.TextureAtlas

object Assets {
  private var maybeAtlas: Option[TextureAtlas] = None

  def load(): Unit = {
    maybeAtlas = Some(
      new TextureAtlas(Gdx.files.internal("assets/atlas/my_atlas.atlas"))
    )

    maybeAtlas.get.getTextures.forEach(
      _.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    )
  }

  def atlas: TextureAtlas = maybeAtlas.get
}
