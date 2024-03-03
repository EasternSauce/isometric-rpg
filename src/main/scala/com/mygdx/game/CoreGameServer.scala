package com.mygdx.game

import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.esotericsoftware.kryonet.{Connection, KryoSerialization, Listener, Server}
import com.mygdx.game.gamestate.GameState
import com.mygdx.game.screen.ServerCamScreen
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

object CoreGameServer extends CoreGame {
  override val endPoint: Server = {
    val kryo: Kryo = {
      val instantiator = new ScalaKryoInstantiator
      instantiator.setRegistrationRequired(false)
      instantiator.newKryo()
    }

    new Server(16384 * 1000, 2048 * 100, new KryoSerialization(kryo))
  }

  private def server: Server = endPoint

  override val playScreen: Screen = ServerCamScreen(gameplay)

  private def runServer(): Unit = {
    server.start()
    server.bind(54555, 54777)

    server.addListener(new Listener() {
      override def received(connection: Connection, obj: Any): Unit = {
        obj match {
          case gs: GameState => println(gs)
          case _             =>
        }
      }
    })
  }

  def main(arg: Array[String]): Unit = {
    new Thread(new Runnable() {
      override def run(): Unit = {
        runServer()
      }
    }).start()

    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Drop")
    config.setWindowedMode(Constants.WindowWidth, Constants.WindowHeight)
    config.useVsync(true)
    config.setForegroundFPS(60)
    new Lwjgl3Application(CoreGameServer, config)
  }

  override def onCreate(): Unit = {
    GameDataBroadcaster.start(server)
  }
}
