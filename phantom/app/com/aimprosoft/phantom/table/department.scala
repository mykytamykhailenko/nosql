package com.aimprosoft.phantom.table

import com.aimprosoft.model.Department
import com.outworkers.phantom.dsl._

import scala.concurrent.duration.Duration

abstract class department extends Table[department, Department[UUID]] {

  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with ClusteringOrder

  object description extends StringColumn with ClusteringOrder

  val prepInsert =
    insert()
      .p_value(_.id, ?)
      .p_value(_.name, ?)
      .p_value(_.description, ?)
      .prepare()

  // equivalent of writing USING TIMESTAMP
  def insertAt(duration: Duration) =
    insert()
      .timestamp(duration)
      .p_value(_.id, ?)
      .p_value(_.name, ?)
      .p_value(_.description, ?)
      .prepare()

  def deleteAt(duration: Duration) =
    delete
      .where(_.id eqs ?)
      .timestamp(duration)
      .prepare()

}
