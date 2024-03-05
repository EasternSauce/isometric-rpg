package com.mygdx.game.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.softwaremill.quicklens.ModifyPimp

object DamageDealingUtils {
  def registerLastAttackedByCreature(
      sourceCreatureId: EntityId[Creature]
  ): Creature => Creature = { creature =>
    creature
      .modify(_.params.lastAttackedTimer)
      .using(_.restart())
      .modify(_.params.currentTargetId)
      .setTo(Some(sourceCreatureId))
  }

  def dealDamageToCreature(damage: Float): Creature => Creature = { creature =>
    if (creature.params.life - damage > 0) {
      creature
        .modify(_.params.life)
        .setTo(creature.params.life - damage)
    } else {
      creature.modify(_.params.life).setTo(0)
    }
  }
}
