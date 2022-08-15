package com.aimprosoft.slick.module

import cats.Monad
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.{Department, Employee}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.inter.BasicOpSlickInterp.SlickActionLang
import com.aimprosoft.slick.inter.SlickMatLang
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import com.google.inject.{AbstractModule, TypeLiteral}
import slickeffect.implicits.dbioCatsEffectSync

import scala.concurrent.ExecutionContext.Implicits.global

class PlayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[BasicActionLang[DBIO, Employee]] {}).toInstance(SlickActionLang(employeeTable))
    bind(new TypeLiteral[BasicActionLang[DBIO, Department]] {}).toInstance(SlickActionLang(departmentTable))
    bind(new TypeLiteral[MatLang[DBIO]] {}).to(new TypeLiteral[SlickMatLang] {})
    bind(new TypeLiteral[Monad[DBIO]] {}).toInstance(implicitly[Monad[DBIO]])

  }
}