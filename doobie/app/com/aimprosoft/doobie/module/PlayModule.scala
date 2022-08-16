package com.aimprosoft.doobie.module

import cats.Monad
import cats.effect.IO
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.dao.DoobieDAO
import com.aimprosoft.doobie.helpers.GetFields._
import com.aimprosoft.doobie.mat.IOMaterializer
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model._
import com.google.inject.{AbstractModule, TypeLiteral}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.{global => globalScheduler}
import zio.interop.catz._

class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    bind(new TypeLiteral[BasicDAO[IO, Employee]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[BasicDAO[IO, Department]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[Materializer[IO]] {}).toInstance(IOMaterializer())
    bind(new TypeLiteral[Monad[IO]] {}).toInstance(implicitly[Monad[IO]])

    bind(new TypeLiteral[BasicDAO[Task, Employee]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[BasicDAO[Task, Department]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[Materializer[Task]] {}).toInstance(mat.TaskMaterializer(globalScheduler))
    bind(new TypeLiteral[Monad[Task]] {}).toInstance(implicitly[Monad[Task]])

    bind(new TypeLiteral[BasicDAO[zio.Task, Employee]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[BasicDAO[zio.Task, Department]] {}).toInstance(DoobieDAO())
    bind(new TypeLiteral[Materializer[zio.Task]] {}).toInstance(mat.ZIOMaterializer(zio.Runtime.default))
    bind(new TypeLiteral[Monad[zio.Task]] {}).toInstance(implicitly[Monad[zio.Task]])


  }
}