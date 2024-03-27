package com.mygdx.game.input

import com.badlogic.gdx.Gdx
import com.mygdx.game.Constants
import com.mygdx.game.util.Vector2
import com.mygdx.game.view.IsometricProjection

case class Input(
    mousePos: Vector2,
    attackButtonJustPressed: Boolean,
    moveButtonPressed: Boolean,
    inventoryToggleKeyJustPressed: Boolean
) {
  def mouseWorldPos(playerPos: Vector2): Vector2 = {
    val mouseScreenPos =
      IsometricProjection.translatePosScreenToIso(mousePos)

    playerPos.add(mouseScreenPos)
  }
}

object Input {
  def poll(): Input = {
    Input(
      mousePos = getMousePos,
      attackButtonJustPressed =
        Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.RIGHT),
      moveButtonPressed =
        Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT),
      inventoryToggleKeyJustPressed =
        Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)
    )
  }

  private def getMousePos: Vector2 = {
    val mouseX =
      Gdx.input.getX * Constants.ViewportWorldWidth / Gdx.graphics.getWidth
    val mouseY =
      Constants.ViewportWorldHeight - (Gdx.input.getY * Constants.ViewportWorldHeight / Gdx.graphics.getHeight)

    val mouseCenterX = mouseX - Constants.ViewportWorldWidth / 2f
    val mouseCenterY = mouseY - Constants.ViewportWorldHeight / 2f

    Vector2(mouseCenterX, mouseCenterY)
  }
}
