package com.mygdx.game.command

import com.mygdx.game.gamestate.event.broadcast.BroadcastEvent

case class ActionsPerformCommand(actions: List[BroadcastEvent])
    extends GameCommand
