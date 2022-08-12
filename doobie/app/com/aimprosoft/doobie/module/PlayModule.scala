package com.aimprosoft.doobie.module

import cats.Monad
import cats.effect.{ContextShift, IO}
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model._
import com.aimprosoft.doobie.getTransactor
import com.aimprosoft.doobie.helpers.GetFields._
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.{IOMatLang, TaskMatLang, ZIOMatLang}
import com.google.inject.{AbstractModule, TypeLiteral}
import monix.eval.Task

import scala.concurrent.ExecutionContext.{global => globalContext}
import monix.execution.Scheduler.Implicits.{global => globalScheduler}
import zio.interop.catz._

class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(globalContext)

    bind(new TypeLiteral[BasicActionLang[IO, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicActionLang[IO, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[IO]] {}).toInstance(IOMatLang())
    bind(new TypeLiteral[Monad[IO]] {}).toInstance(implicitly[Monad[IO]])


    implicit val contextShiftTask: ContextShift[Task] = Task.contextShift

    bind(new TypeLiteral[BasicActionLang[Task, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicActionLang[Task, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[Task]] {}).toInstance(TaskMatLang(globalScheduler))
    bind(new TypeLiteral[Monad[Task]] {}).toInstance(implicitly[Monad[Task]])

    bind(new TypeLiteral[BasicActionLang[zio.Task, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicActionLang[zio.Task, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[zio.Task]] {}).toInstance(ZIOMatLang(zio.Runtime.default))
    bind(new TypeLiteral[Monad[zio.Task]] {}).toInstance(implicitly[Monad[zio.Task]])

  }
}