package com.mygdx.game.input

import com.badlogic.gdx.Gdx
import com.mygdx.game.Constants
import com.mygdx.game.util.Vector2

case class Input(
    mousePos: Vector2,
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

  private def getMousePos: Vector2 = {
    val mouseX =
      Gdx.input.getX * Constants.ViewpointWorldWidth / Gdx.graphics.getWidth
    val mouseY =
      Constants.ViewpointWorldHeight - (Gdx.input.getY * Constants.ViewpointWorldHeight / Gdx.graphics.getHeight)

    val mouseCenterX = mouseX - Constants.ViewpointWorldWidth / 2f
    val mouseCenterY = mouseY - Constants.ViewpointWorldHeight / 2f

    Vector2(mouseCenterX, mouseCenterY)
  }
}
