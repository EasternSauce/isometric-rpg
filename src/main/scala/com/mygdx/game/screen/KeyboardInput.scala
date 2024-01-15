package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys

case class KeyboardInput(
    moveWest: Boolean,
    moveEast: Boolean,
    moveSouth: Boolean,
    moveNorth: Boolean
) {}

object KeyboardInput {
  def getInput(): KeyboardInput = {
    KeyboardInput(
      moveWest = Gdx.input.isKeyPressed(Keys.A),
      moveEast = Gdx.input.isKeyPressed(Keys.D),
      moveSouth = Gdx.input.isKeyPressed(Keys.S),
      moveNorth = Gdx.input.isKeyPressed(Keys.W)
    )
  }
}
