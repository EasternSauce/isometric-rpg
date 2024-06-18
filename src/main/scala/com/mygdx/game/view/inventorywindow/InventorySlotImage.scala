package com.mygdx.game.view.inventorywindow

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image

case class InventorySlotImage(
    atlasRegion: TextureAtlas.AtlasRegion,
    pos: Int
) extends Image(atlasRegion)
