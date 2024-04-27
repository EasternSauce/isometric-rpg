package com.mygdx.game.gamestate.item

case class Item(template: ItemTemplate, quantity: Integer = 1) {
  def info: String = template.name + "\n..."
}
