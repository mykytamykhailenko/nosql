package com.aimprosoft.phantom.table

import com.aimprosoft.model.Employee
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class employee_by_department_id extends Table[employee_by_department_id, Employee[UUID]] {

  object department_id extends UUIDColumn with PartitionKey

  object surname extends StringColumn with ClusteringOrder

  object name extends StringColumn with ClusteringOrder

  object id extends UUIDColumn with ClusteringOrder

  def getIdsByDepartmentId(departmentId: UUID): Future[List[UUID]] =
    select(_.id)
      .where(_.department_id eqs ?)
      .prepare()
      .bind(departmentId)
      .fetch()

/*
  def insertPrepared(employee: Employee[UUID]) = {
    val worker = (employee.id.get, employee.departmentId, employee.name, employee.surname)

    insert
      .p_value(_.id, ?)
      .p_value(_.department_id, ?)
      .p_value(_.name, ?)
      .p_value(_.surname, ?)
      .prepare()
      .bind(worker)
  }
*/

}