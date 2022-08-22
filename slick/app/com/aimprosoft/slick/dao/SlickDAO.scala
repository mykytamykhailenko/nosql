package com.aimprosoft.slick.dao

import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Id}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.table.TSlickBaseTable

import scala.concurrent.ExecutionContext

case class SlickDAO[M <: Id[Int], T <: Table[M] with TSlickBaseTable[M]](entities: TableQuery[T])(implicit ec: ExecutionContext) extends BasicDAO[DBIO, Int, M] {

  def create(value: M): DBIO[Option[Int]] = {

    val nothing: DBIO[Option[Int]] = DBIO.successful(None)
    val created: DBIO[Option[Int]] = entities returning entities.map(_.id.?) += value

    value.id.fold(created)(_ => nothing)
  }

  def update(value: M): DBIO[Option[Affected]] = {

    val nothing: DBIO[Option[Affected]] = DBIO.successful(None)

    val updated: Int => DBIO[Option[Affected]] = (id: Int) =>
      entities
        .filter(_.id === id)
        .update(value)
        .map(Some.apply)

    value.id.fold(nothing)(updated)
  }

  def readAll(): DBIO[Seq[M]] = entities.result

  def readById(id: Int): DBIO[Option[M]] = entities.filter(_.id === id).result.headOption

  def deleteById(id: Int): DBIO[Affected] = entities.filter(_.id === id).delete

}
