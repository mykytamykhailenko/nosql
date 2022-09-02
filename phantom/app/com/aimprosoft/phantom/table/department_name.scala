package com.aimprosoft.phantom.table

import com.outworkers.phantom.dsl._

import scala.concurrent.duration.Duration

abstract class department_name extends Table[department_name, String] {

  object name extends StringColumn with PartitionKey

  override def fromRow(row: Row): String = name(row)

  lazy val prepInsert = insert().p_value(_.name, ?).prepare()

  def insertAt(duration: Duration) = insert().p_value(_.name, ?).timestamp(duration).prepare()

  def deleteAt(duration: Duration) = delete().where(_.name eqs ?).timestamp(duration).prepare()

}
