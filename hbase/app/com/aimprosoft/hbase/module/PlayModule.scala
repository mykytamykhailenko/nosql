package com.aimprosoft.hbase.module

import cats.Monad
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.hbase.dao.DepartmentDAO
import com.aimprosoft.hbase.service.{DepartmentService, EmployeeService}
import com.aimprosoft.mat.Materializer
import com.aimprosoft.mat.Materializer.futureMaterializer
import com.aimprosoft.model.Department
import com.aimprosoft.service.{TDepartmentService, TEmployeeService}
import com.google.inject.{AbstractModule, TypeLiteral}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlayModule extends AbstractModule {
  override def configure(): Unit = {

    bind(new TypeLiteral[Materializer[Future]] {}).toInstance(futureMaterializer)

    bind(new TypeLiteral[Monad[Future]] {}).toInstance(implicitly[Monad[Future]])

    bind(new TypeLiteral[TEmployeeService[Future, UUID]] {}).to(classOf[EmployeeService])
    bind(new TypeLiteral[TDepartmentService[Future, UUID]] {}).to(classOf[DepartmentService])

    bind(new TypeLiteral[BasicDAO[Future, UUID, Department[UUID]]] {}).to(classOf[DepartmentDAO])
  }
}