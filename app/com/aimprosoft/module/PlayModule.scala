package com.aimprosoft.module

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.aimprosoft.doobie.getTransactor
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.DoobieMatLang
import com.aimprosoft.lang.{BasicActionLang, MatLang}
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.inter.BasicOpSlickInterp.SlickActionLang
import com.aimprosoft.slick.inter.SlickMatLang
import com.aimprosoft.slick.table.{DepartmentTable, EmployeeTable}
import com.google.inject.{AbstractModule, TypeLiteral}

import scala.concurrent.ExecutionContext

// Unfortunately, scala-guice is not compatible with Play.
class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
    implicit val context: ExecutionContext = runtime.compute

    bind(classOf[IORuntime]).toInstance(runtime)

    bind(new TypeLiteral[BasicActionLang[DBIO, Employee]] {}).toInstance(SlickActionLang(TableQuery[EmployeeTable]))
    bind(new TypeLiteral[BasicActionLang[DBIO, Department]] {}).toInstance(SlickActionLang(TableQuery[DepartmentTable]))

    bind(new TypeLiteral[BasicActionLang[IO, Employee]] {}).toInstance(DoobieActionLang[IO, Employee]())
    bind(new TypeLiteral[BasicActionLang[IO, Department]] {}).toInstance(DoobieActionLang[IO, Department]())

    bind(new TypeLiteral[MatLang[DBIO]] {}).to(new TypeLiteral[SlickMatLang] {})
    bind(new TypeLiteral[MatLang[IO]] {}).toInstance(DoobieMatLang(runtime))

    bind(new TypeLiteral[Monad[DBIO]] {}).toInstance(slickeffect.implicits.dbioCatsEffectSync)
    bind(new TypeLiteral[Monad[IO]] {}).toInstance(implicitly[Monad[IO]])


  }
}