package com.mygdx.game.gamestate.event.gamestate

import com.mygdx.game.gamestate.creature.Creature
import com.mygdx.game.gamestate.event.GameStateEvent
import com.mygdx.game.gamestate.{EntityId, GameState}
import com.mygdx.game.util.Chaining.customUtilChainingOps
import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}

case class CreatureDeathGameStateEvent(creatureId: EntityId[Creature])
    extends GameStateEvent {
  def applyToGameState(gameState: GameState): GameState = {
    gameState
      .modify(_.creatures.at(creatureId))
      .using(creature =>
        creature
          .modify(_.params.deathAcknowledged)
          .setTo(true)
          .modify(_.params.deathAnimationTimer)
          .using(_.restart())
          .modify(_.params.attackAnimationTimer)
          .using(_.stop())
          .pipeIf(_.params.player)(
            _.modify(_.params.respawnTimer)
              .using(_.restart())
          )
      )
  }

}
