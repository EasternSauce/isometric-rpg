package com.mygdx.game.util
import scala.language.implicitConversions

object Chaining extends CustomChainingSyntax

trait CustomChainingSyntax {
  @`inline` implicit final def customUtilChainingOps[A](
      a: A
  ): CustomChainingOps[A] = new CustomChainingOps(a)
}

final class CustomChainingOps[A](private val self: A) extends AnyVal {
  def pipeIf(cond: A => Boolean)(f: A => A): A = {
    if (cond(self)) f(self)
    else self
  }
}
