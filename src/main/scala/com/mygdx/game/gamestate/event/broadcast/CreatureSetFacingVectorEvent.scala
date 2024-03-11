//package com.mygdx.game.gamestate.event.broadcast
//
//import com.mygdx.game.gamestate.creature.Creature
//import com.mygdx.game.gamestate.{EntityId, GameState}
//import com.mygdx.game.util.Vector2
//import com.softwaremill.quicklens.{ModifyPimp, QuicklensMapAt}
//
//case class CreatureSetFacingVectorEvent(
//    creatureId: EntityId[Creature],
//    facingVector: Vector2
//) extends BroadcastEvent {
//
//  override def applyToGameState(gameState: GameState): GameState = {
//    if (gameState.creatures.contains(creatureId)) {
//      gameState
//        .modify(_.creatures.at(creatureId).params.facingVector)
//        .setTo(facingVector)
//    } else {
//      gameState
//    }
//  }
//}
