package com.mygdx.game.command

import com.mygdx.game.gamestate.event.GameStateEvent

case class ActionsPerformCommand(actions: List[GameStateEvent])
    extends GameCommand
