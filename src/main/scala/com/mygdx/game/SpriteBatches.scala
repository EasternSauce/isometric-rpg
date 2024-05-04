package com.mygdx.game

import com.mygdx.game.view.SpriteBatch

case class SpriteBatches(
    worldSpriteBatch: SpriteBatch = SpriteBatch(),
    worldTextSpriteBatch: SpriteBatch = SpriteBatch(),
    hudBatch: SpriteBatch = SpriteBatch()
)
