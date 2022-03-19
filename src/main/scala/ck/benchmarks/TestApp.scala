package ck.benchmarks

import cats.data.Chain
import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._
import org.atnos.eff.Eff
import org.atnos.eff.syntax.all._

object TestApp extends App {
  println(testZPure.provide(Env("config")).runAll(State(2)))
  println(testMTLChunk[P4].provide(Env("config")).runAll(State(2)))
  println(testMTL[P3].run(Env("config"), State(2)))
  println(
    Eff.run(
      testEff
        .runReader(Env("config"))
        .runState(State(2))
        .runEither[Throwable]
        .runWriter[Chain[Event]]
    )
  )
}
