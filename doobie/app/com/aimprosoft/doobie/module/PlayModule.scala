package com.aimprosoft.doobie.module

import cats.Monad
import cats.effect.IO
import com.aimprosoft.common.lang.{BasicDAO, MatLang}
import com.aimprosoft.common.model._
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.helpers.GetFields._
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.{IOMatLang, TaskMatLang, ZIOMatLang}
import com.google.inject.{AbstractModule, TypeLiteral}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.{global => globalScheduler}
import zio.interop.catz._

class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    bind(new TypeLiteral[BasicDAO[IO, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicDAO[IO, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[IO]] {}).toInstance(IOMatLang())
    bind(new TypeLiteral[Monad[IO]] {}).toInstance(implicitly[Monad[IO]])

    bind(new TypeLiteral[BasicDAO[Task, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicDAO[Task, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[Task]] {}).toInstance(TaskMatLang(globalScheduler))
    bind(new TypeLiteral[Monad[Task]] {}).toInstance(implicitly[Monad[Task]])

    bind(new TypeLiteral[BasicDAO[zio.Task, Employee]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[BasicDAO[zio.Task, Department]] {}).toInstance(DoobieActionLang())
    bind(new TypeLiteral[MatLang[zio.Task]] {}).toInstance(ZIOMatLang(zio.Runtime.default))
    bind(new TypeLiteral[Monad[zio.Task]] {}).toInstance(implicitly[Monad[zio.Task]])

  }
}