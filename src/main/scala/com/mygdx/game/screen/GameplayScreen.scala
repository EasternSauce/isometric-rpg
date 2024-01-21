package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.Gameplay

object GameplayScreen extends Screen {

  private val gameplay: Gameplay = Gameplay()

//  val server: Server = {
//    val kryo: Kryo = {
//      val instantiator = new ScalaKryoInstantiator
//      instantiator.setRegistrationRequired(false)
//      instantiator.newKryo()
//    }
//
//    new Server(16384, 2048, new KryoSerialization(kryo))
//  }
//
//  val client: Client = {
//    val kryo: Kryo = {
//      val instantiator = new ScalaKryoInstantiator
//      instantiator.setRegistrationRequired(false)
//      instantiator.newKryo()
//    }
//
//    new Client(8192, 2048, new KryoSerialization(kryo))
//  }
//
//  private def runServer(): Unit = {
//    server.start()
//    server.bind(54555, 54777)
//
//    server.addListener(new Listener() {
//      override def received(connection: Connection, obj: Any): Unit = {
//        obj match {
//          case gs: GameState =>
//          case _ =>
//        }
//      }
//    })
//  }

  override def show(): Unit = {
    gameplay.init()

//    new Thread(new Runnable() {
//      override def run(): Unit = {
//        runServer()
//      }
//    }).start()
//
//    Thread.sleep(1000)
//
//    client.start()
//    client.connect(5000, "localhost", 54555, 54777)
//    client.sendTCP(gameState)

  }

  override def render(delta: Float): Unit = {
    gameplay.update(delta)
  }

  override def dispose(): Unit = {
    gameplay.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    gameplay.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
