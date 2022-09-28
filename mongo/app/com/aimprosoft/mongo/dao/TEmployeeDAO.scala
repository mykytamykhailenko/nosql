package com.aimprosoft.mongo.dao

import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.Employee
import com.google.inject.ImplementedBy

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[EmployeeDAO])
trait TEmployeeDAO extends BasicDAO[Future, UUID, Employee[UUID]] {

  def getEmployeesByDepartmentId(id: UUID): Future[Seq[Employee[UUID]]]

}
