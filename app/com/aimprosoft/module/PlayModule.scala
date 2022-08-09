package com.aimprosoft.module

import cats.Monad
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.lang.{BasicActionLang, MatLang}
import com.google.inject.{AbstractModule, TypeLiteral}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.inter.BasicOpSlickInterp.{departmentSlickInterp, employeeSlickInterp}
import com.aimprosoft.slick.inter.SlickMatLang

import scala.concurrent.ExecutionContext.Implicits.global

// Unfortunately, scala-guice is not compatible with Play.
class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    bind(new TypeLiteral[BasicActionLang[DBIO, Employee]]{}).toInstance(employeeSlickInterp)
    bind(new TypeLiteral[BasicActionLang[DBIO, Department]]{}).toInstance(departmentSlickInterp)

    bind(new TypeLiteral[MatLang[DBIO]]{}).to(new TypeLiteral[SlickMatLang] {})

    bind(new TypeLiteral[Monad[DBIO]]{}).toInstance(slickeffect.implicits.dbioCatsEffectSync)
  }
}