package com.mygdx.game.util

import com.softwaremill.quicklens.ModifyPimp

case class SimpleTimer private (time: Float = 0f, running: Boolean) {
  def start(): SimpleTimer = this.modify(_.running).setTo(true)

  def stop(): SimpleTimer =
    this.modify(_.time).setTo(0f).modify(_.running).setTo(false)

  def restart(): SimpleTimer =
    this.modify(_.time).setTo(0f).modify(_.running).setTo(true)

  def update(delta: Float): SimpleTimer =
    this.modify(_.time).usingIf(running)(_ + delta)

}
