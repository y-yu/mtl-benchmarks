package ck.benchmarks

import cats.Monad
import cats.data.{ Chain, IndexedReaderWriterStateT, Reader, State => CatsState, Writer }
import cats.effect.SyncIO
import cats.implicits._
import cats.mtl._
import ck.benchmarks.ZioInstances.ZIOReaderWriterState
import org.atnos.eff.{ Eff, Fx }
import org.atnos.eff.all.{ ask, modify, tell => effTell }
import zio.prelude.fx.ZPure

object Test {
  case class Env(config: String)
  case class Event(name: String)
  case class State(value: Int)

  type P[+A] = ZIOReaderWriterState[Env, Chain[Event], State, A]
  type P2[A] = IndexedReaderWriterStateT[SyncIO, Env, Chain[Event], State, State, A]
  type P3[A] = IndexedReaderWriterStateT[Either[Throwable, *], Env, Chain[Event], State, State, A]
  type P4[A] = ZPure[Event, State, State, Env, Throwable, A]
  type P5 = Fx.fx4[Reader[Env, *], CatsState[State, *], Either[Throwable, *], Writer[Chain[Event], *]]

  val loops = 1000

  def testReaderWriterState[F[_]: Monad]: IndexedReaderWriterStateT[F, Env, Chain[Event], State, State, Unit] =
    (1 to loops).toList
      .traverse(_ =>
        for {
          conf <- IndexedReaderWriterStateT.ask[F, Env, Chain[Event], State].map(_.config)
          _    <- IndexedReaderWriterStateT.tell[F, Env, Chain[Event], State](Chain(Event(s"Env = $conf")))
          _ <- IndexedReaderWriterStateT.modify[F, Env, Chain[Event], State, State](state =>
                state.copy(value = state.value + 1)
              )
        } yield ()
      )
      .void

  def testZPure: ZPure[Event, State, State, Env, Throwable, Unit] =
    ZPure
      .forEach((1 to loops).toList)(_ =>
        for {
          conf <- ZPure.access[Env](_.config)
          _    <- ZPure.log(Event(s"Env = $conf"))
          _    <- ZPure.update[State, State](state => state.copy(value = state.value + 1))
        } yield ()
      )
      .unit

  def testEff: Eff[P5, Unit] =
    Eff.traverseA((1 to loops).toList)( _ =>
      for {
        conf <- ask[P5, Env].map(_.config)
        _    <- effTell[P5, Chain[Event]](Chain(Event(s"Env = $conf")))
        _    <- modify[P5, State](state => state.copy(value = state.value + 1))
      } yield ()
    )
    .void

  def testMTL[F[_]: Monad](
    implicit reader: Ask[F, Env],
    writer: Tell[F, Chain[Event]],
    state: Stateful[F, State]
  ): F[Unit] =
    (1 to loops).toList
      .traverse(_ =>
        for {
          conf <- reader.ask.map(_.config)
          _    <- writer.tell(Chain(Event(s"Env = $conf")))
          _    <- state.modify(state => state.copy(value = state.value + 1))
        } yield ()
      )
      .void

  def testMTLChunk[F[_]: Monad](
    implicit reader: Ask[F, Env],
    writer: Tell[F, Event],
    state: Stateful[F, State]
  ): F[Unit] =
    (1 to loops).toList
      .traverse(_ =>
        for {
          conf <- reader.ask.map(_.config)
          _    <- writer.tell(Event(s"Env = $conf"))
          _    <- state.modify(state => state.copy(value = state.value + 1))
        } yield ()
      )
      .void
}
