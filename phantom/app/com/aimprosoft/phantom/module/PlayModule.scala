package com.aimprosoft.phantom.module

import cats.Monad
import cats.implicits.catsStdInstancesForFuture
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.mat.Materializer
import com.aimprosoft.mat.Materializer.futureMaterializer
import com.aimprosoft.model.Department
import com.aimprosoft.phantom.dao.DepartmentDAO
import com.aimprosoft.phantom.service.{DepartmentService, EmployeeService}
import com.aimprosoft.service.{TDepartmentService, TEmployeeService}
import com.google.inject.{AbstractModule, TypeLiteral}
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

class PlayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[Materializer[Future]] {}).toInstance(futureMaterializer)
    bind(new TypeLiteral[Monad[Future]] {}).toInstance(implicitly[Monad[Future]])

    bind(new TypeLiteral[TEmployeeService[Future, UUID]] {}).to(classOf[EmployeeService])
    bind(new TypeLiteral[TDepartmentService[Future, UUID]] {}).to(classOf[DepartmentService])

    bind(new TypeLiteral[BasicDAO[Future, UUID, Department[UUID]]] {}).to(classOf[DepartmentDAO])

    bind(classOf[CassandraConnection]).toInstance(ContactPoint.local.keySpace("divisions"))
  }
}