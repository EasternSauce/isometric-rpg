package com.mygdx.game

import com.mygdx.game.gamestate.EntityId
import com.mygdx.game.gamestate.ability.Ability
import com.mygdx.game.gamestate.creature.Creature

case class ModelEventsScheduler() {
  private var creatureModelAddedQueue: List[EntityId[Creature]] = List()
  private var creatureModelRemovedQueue: List[EntityId[Creature]] = List()
  private var abilityModelAddedQueue: List[EntityId[Ability]] = List()
  private var abilityModelRemovedQueue: List[EntityId[Ability]] = List()

  def scheduleCreatureModelAdded(creatureId: EntityId[Creature]): Unit = {
    creatureModelAddedQueue = creatureModelAddedQueue.appended(creatureId)
  }

  def scheduleCreatureModelRemoved(creatureId: EntityId[Creature]): Unit = {
    creatureModelRemovedQueue = creatureModelRemovedQueue.appended(creatureId)
  }

  def scheduleAbilityModelAdded(abilityId: EntityId[Ability]): Unit = {
    abilityModelAddedQueue = abilityModelAddedQueue.appended(abilityId)
  }

  def scheduleAbilityModelRemoved(abilityId: EntityId[Ability]): Unit = {
    abilityModelRemovedQueue = abilityModelRemovedQueue.appended(abilityId)
  }

  def pollCreatureModelAdded(): List[EntityId[Creature]] = {
    val temp = creatureModelAddedQueue
    creatureModelAddedQueue = List()
    temp
  }

  def pollCreatureModelRemoved(): List[EntityId[Creature]] = {
    val temp = creatureModelRemovedQueue
    creatureModelRemovedQueue = List()
    temp
  }

  def pollAbilityModelAdded(): List[EntityId[Ability]] = {
    val temp = abilityModelAddedQueue
    abilityModelAddedQueue = List()
    temp
  }

  def pollAbilityModelRemoved(): List[EntityId[Ability]] = {
    val temp = abilityModelRemovedQueue
    abilityModelRemovedQueue = List()
    temp
  }

}
