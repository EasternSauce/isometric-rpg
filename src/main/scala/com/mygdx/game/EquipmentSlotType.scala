package com.mygdx.game

object EquipmentSlotType extends Enumeration {
  type EquipmentSlotType = Value

  protected case class EquipmentSlotTypeDetails(
      i: Int,
      name: String,
      displayName: String
  ) extends super.Val(i, name)

  import scala.language.implicitConversions

  implicit def valueToFingerDetails(x: Value): EquipmentSlotTypeDetails =
    x.asInstanceOf[EquipmentSlotTypeDetails]

  val Weapon: Value = EquipmentSlotTypeDetails(0, "Weapon", "weapon")
  val Shield: Value = EquipmentSlotTypeDetails(1, "Shield", "shield")
  val Ring: Value = EquipmentSlotTypeDetails(2, "Ring", "ring")
  val BodyArmor: Value = EquipmentSlotTypeDetails(3, "BodyArmor", "body armor")
  val Gloves: Value = EquipmentSlotTypeDetails(4, "Gloves", "gloves")
  val Boots: Value = EquipmentSlotTypeDetails(5, "Boots", "boots")
}
