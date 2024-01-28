package com.mygdx.game.input

import com.badlogic.gdx.Gdx
import com.mygdx.game.Constants

case class Input(
    mousePos: (Float, Float),
    attackButtonJustPressed: Boolean,
    moveButtonPressed: Boolean
)

object Input {
  def poll(): Input = {
    Input(
      getMousePos,
      Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.RIGHT),
      Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)
    )
  }

  private def getMousePos: (Float, Float) = {
    val mouseX =
      Gdx.input.getX * Constants.ViewpointWorldWidth / Gdx.graphics.getWidth
    val mouseY =
      Constants.ViewpointWorldHeight - (Gdx.input.getY * Constants.ViewpointWorldHeight / Gdx.graphics.getHeight)

    val mouseCenterX = mouseX - Constants.ViewpointWorldWidth / 2f
    val mouseCenterY = mouseY - Constants.ViewpointWorldHeight / 2f
    (mouseCenterX, mouseCenterY)
  }
}
