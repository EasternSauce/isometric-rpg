package com.mygdx.game.command

case class RegisterClientRequestCommand(clientId: Option[String])
    extends GameCommand
