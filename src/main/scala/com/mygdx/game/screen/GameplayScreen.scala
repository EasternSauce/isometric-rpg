package com.mygdx.game.screen

import com.badlogic.gdx.Screen
import com.mygdx.game.gamestate.GameState
object GameplayScreen extends Screen {

  private val clientInformation: ClientInformation =
    ClientInformation(clientCreatureId = "creature1")

  private val view: View = View(clientInformation)
  private val spriteBatch: SpriteBatch = SpriteBatch()
  private var gameState: GameState = _

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
    gameState = GameState.initialState(clientInformation)

    view.init(clientInformation, gameState)
    spriteBatch.init()

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
    val (playerPosX, playerPosY) = view.getPlayerPos
    gameState = gameState.update(
      clientInformation,
      playerPosX,
      playerPosY,
      delta
    )

    view.update(clientInformation, gameState)

    view.draw(spriteBatch, gameState)
  }

  override def dispose(): Unit = {
    spriteBatch.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    view.resize(width, height)
  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}
