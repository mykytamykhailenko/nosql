package com.aimprosoft.phantom.table

import com.aimprosoft.model.Employee
import com.outworkers.phantom.dsl._

abstract class employee extends Table[employee, Employee[UUID]] {

  object id extends UUIDColumn with PartitionKey

  object surname extends StringColumn with ClusteringOrder

  object name extends StringColumn with ClusteringOrder

  object department_id extends UUIDColumn
/*
  def readById(id: UUID) = select.where(_.id eqs ?).prepare().bind(id)

  def store(employee: Employee[UUID]) = {

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
