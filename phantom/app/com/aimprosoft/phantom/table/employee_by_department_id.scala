package com.aimprosoft.phantom.table

import com.aimprosoft.model.Employee
import com.outworkers.phantom.dsl._

import scala.concurrent.duration.Duration

abstract class employee_by_department_id extends Table[employee_by_department_id, Employee[UUID]] {

  object department_id extends UUIDColumn with PartitionKey

  object surname extends StringColumn with ClusteringOrder

  object name extends StringColumn with ClusteringOrder

  object id extends UUIDColumn with ClusteringOrder

  lazy val prepInsert =
    insert()
      .p_value(_.id, ?)
      .p_value(_.department_id, ?)
      .p_value(_.name, ?)
      .p_value(_.surname, ?)
      .prepare()

  def insertAt(duration: Duration) =
    insert()
      .timestamp(duration)
      .p_value(_.id, ?)
      .p_value(_.department_id, ?)
      .p_value(_.name, ?)
      .p_value(_.surname, ?)
      .prepare()

  def deleteAt(duration: Duration) =
    delete
      .where(_.id eqs ?)
      .timestamp(duration)
      .prepare()

}