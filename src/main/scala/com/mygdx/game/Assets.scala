package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{TextureAtlas, TextureRegion}

object Assets {
  private var maybeAtlas: Option[TextureAtlas] = None

  private var icons: Array[Array[TextureRegion]] = _

  def load(): Unit = {
    maybeAtlas = Some(
      new TextureAtlas(Gdx.files.internal("assets/atlas/my_atlas.atlas"))
    )

    maybeAtlas.get.getTextures.forEach(
      _.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    )

    icons =
      new TextureRegion(Assets.getTexture("icons/nice_icons")).split(32, 32)
  }

  var textureMap: Map[String, Texture] = Map[String, Texture]()

  def getTexture(texturePath: String): Texture = {
    if (textureMap.contains(texturePath)) {
      textureMap(texturePath)
    } else {
      textureMap = textureMap.updated(
        texturePath,
        new Texture(Gdx.files.internal("assets/" + texturePath + ".png"))
      )
      textureMap(texturePath)
    }
  }

  def getIcon(x: Int, y: Int): TextureRegion = icons(y)(x)

  def atlas: TextureAtlas = maybeAtlas.get
}
