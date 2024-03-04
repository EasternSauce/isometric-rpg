package com.mygdx.game

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.esotericsoftware.kryonet.{Client, KryoSerialization}
import com.mygdx.game.action.GameStateAction
import com.mygdx.game.screen.GameplayScreen
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

object CoreGameClient extends CoreGame {

  override val endPoint: Client = {
    val kryo: Kryo = {
      val instantiator = new ScalaKryoInstantiator
      instantiator.setRegistrationRequired(false)
      instantiator.newKryo()

    }
    new Client(8192 * 100, 2048 * 100, new KryoSerialization(kryo))
  }

  private def client: Client = endPoint
  private val listener: ClientListener = ClientListener(this)

  override val playScreen: Screen = GameplayScreen(gameplay, client)

  def main(arg: Array[String]): Unit = {
    client.start()
    client.connect(50000, "localhost", 54555, 54777)

    client.addListener(listener)

    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.useVsync(true)
    config.setForegroundFPS(60)
    new Lwjgl3Application(CoreGameClient, config)
  }

  override def onCreate(): Unit = {}

  override def onGameStateUpdate(actions: List[GameStateAction]): Unit = {}
}
