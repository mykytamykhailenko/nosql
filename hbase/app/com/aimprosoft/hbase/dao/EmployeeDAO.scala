package com.aimprosoft.hbase.dao

import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.model.{Affected, Employee}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class EmployeeDAO @Inject()(connectionLifecycle: AsyncConnectionLifecycle) extends TEmployeeDAO {
  override def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]] = ???

  override def create(value: Employee[UUID]): Future[Option[UUID]] = ???

  override def update(value: Employee[UUID]): Future[Option[Affected]] = ???

  override def readAll(): Future[Seq[Employee[UUID]]] = ???

  override def readById(id: UUID): Future[Option[Employee[UUID]]] = ???

  override def deleteById(id: UUID): Future[Affected] = ???
}
