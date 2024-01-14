package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2

object Assets {
  var atlas: Option[TextureAtlas] = None

  def load(): Unit = {
    atlas = Some(
      new TextureAtlas(Gdx.files.internal("assets/atlas/my_atlas.atlas"))
    )

    atlas.get.getTextures.forEach(_.setFilter(TextureFilter.Linear, TextureFilter.Linear))
  }
}
