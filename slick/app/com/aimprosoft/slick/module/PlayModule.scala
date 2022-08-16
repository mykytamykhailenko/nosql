package com.aimprosoft.slick.module

import cats.Monad
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.slick.dao.SlickDAO
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.mat.SlickMaterializer
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import com.google.inject.{AbstractModule, TypeLiteral}
import slickeffect.implicits.dbioCatsEffectSync

import scala.concurrent.ExecutionContext.Implicits.global

class PlayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[BasicDAO[DBIO, Employee]] {}).toInstance(SlickDAO(employeeTable))
    bind(new TypeLiteral[BasicDAO[DBIO, Department]] {}).toInstance(SlickDAO(departmentTable))
    bind(new TypeLiteral[Materializer[DBIO]] {}).to(new TypeLiteral[SlickMaterializer] {})
    bind(new TypeLiteral[Monad[DBIO]] {}).toInstance(implicitly[Monad[DBIO]])

  }
}