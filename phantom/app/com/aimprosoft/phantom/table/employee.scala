package com.aimprosoft.phantom.table

import com.aimprosoft.model.Employee
import com.outworkers.phantom.dsl._

import scala.concurrent.duration.Duration

abstract class employee extends Table[employee, Employee[UUID]] {

  object id extends UUIDColumn with PartitionKey

  object surname extends StringColumn with ClusteringOrder

  object name extends StringColumn with ClusteringOrder

  object department_id extends UUIDColumn

  lazy val prepInsert =
    insert()
      .p_value(_.id, ?)
      .p_value(_.department_id, ?)
      .p_value(_.name, ?)
      .p_value(_.surname, ?)
      .prepare()

  def insertAt(duration: Duration) =
    insert()
      .p_value(_.id, ?)
      .p_value(_.department_id, ?)
      .p_value(_.name, ?)
      .p_value(_.surname, ?)
      .timestamp(duration)
      .prepare()

  def deleteAt(duration: Duration) =
    delete
      .where(_.id eqs ?)
      .timestamp(duration)
      .prepare()

}
