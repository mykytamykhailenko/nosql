package com.aimprosoft.phantom.table

import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.model.Department
import com.outworkers.phantom.dsl._

import scala.concurrent.duration.Duration

abstract class department extends Table[department, Department[UUID]] {

  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with ClusteringOrder

  object description extends StringColumn with ClusteringOrder

  override def fromRow(row: Row): Department[UUID] = Department(id(row).some, name(row), description(row))

  lazy val prepInsert =
    insert()
      .p_value(_.id, ?)
      .p_value(_.name, ?)
      .p_value(_.description, ?)
      .prepare()

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
