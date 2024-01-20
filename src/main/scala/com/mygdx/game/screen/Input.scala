package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.mygdx.game.Constants

case class Input(
    moveWest: Boolean,
    moveEast: Boolean,
    moveSouth: Boolean,
    moveNorth: Boolean
) {
  def movingDiagonally(): Boolean =
    (moveWest || moveEast) && (moveNorth || moveSouth)
}

object Input {
  def getKeyboardInput(): Input = {
    Input(
      moveWest = Gdx.input.isKeyPressed(Keys.A),
      moveEast = Gdx.input.isKeyPressed(Keys.D),
      moveSouth = Gdx.input.isKeyPressed(Keys.S),
      moveNorth = Gdx.input.isKeyPressed(Keys.W),
    )
  }

  def getMousePos: (Float, Float) = {
    val mouseX = Gdx.input.getX * Constants.ViewpointWorldWidth / Gdx.graphics.getWidth
    val mouseY = Constants.ViewpointWorldHeight - (Gdx.input.getY * Constants.ViewpointWorldHeight / Gdx.graphics.getHeight)

    val mouseCenterX = mouseX - Constants.ViewpointWorldWidth / 2f
    val mouseCenterY = mouseY - Constants.ViewpointWorldHeight / 2f
    (mouseCenterX, mouseCenterY)
  }
}
