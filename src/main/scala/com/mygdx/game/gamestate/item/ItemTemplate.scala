package com.mygdx.game.gamestate.item

import com.mygdx.game.EquipmentSlotType
import com.mygdx.game.EquipmentSlotType.EquipmentSlotType
import com.mygdx.game.util.Vector2Int

case class ItemTemplate(
    id: String,
    name: String,
    iconPos: Vector2Int,
    equipmentSlotType: Option[EquipmentSlotType]
)

object ItemTemplate {
  private val templates: Map[String, ItemTemplate] = Map(
    "shortbow" -> ItemTemplate(
      "shortbow",
      "Short Bow",
      Vector2Int(3, 6),
      Some(EquipmentSlotType.Weapon)
    )
  )

  def getById(id: String): ItemTemplate = templates(id)
}
