package com.aimprosoft.module

import cats.Monad
import com.aimprosoft.controllers.{LangMat, SlickMat}
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.tagless.BasicActionLang
import com.google.inject.{AbstractModule, TypeLiteral}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.table.{DepartmentTable, EmployeeTable}
import com.aimprosoft.slick.tagless.BasicOpSlickInterp.composeSlickInterp

import scala.concurrent.ExecutionContext.Implicits.global

// Unfortunately, scala-guice is not compatible with Play.
class PlayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[BasicActionLang[DBIO, Employee]]{}).toInstance(composeSlickInterp(TableQuery[EmployeeTable]))
    bind(new TypeLiteral[BasicActionLang[DBIO, Department]]{}).toInstance(composeSlickInterp(TableQuery[DepartmentTable]))

    bind(new TypeLiteral[LangMat[DBIO]]{}).to(new TypeLiteral[SlickMat] {})

    bind(new TypeLiteral[Monad[DBIO]]{}).toInstance(slickeffect.implicits.dbioCatsEffectSync)
  }
}