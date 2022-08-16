package com.aimprosoft.slick.dao

import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Id, TIdentity}
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.table.TSlickBaseTable

import scala.concurrent.ExecutionContext

case class SlickDAO[M <: TIdentity, T <: Table[M] with TSlickBaseTable[M]](entities: TableQuery[T])(implicit ec: ExecutionContext) extends BasicDAO[DBIO, M] {

  def create(value: M): DBIO[Option[Id]] = {

    val nothing: DBIO[Option[Id]] = DBIO.successful(None)
    val created: DBIO[Option[Id]] = entities returning entities.map(_.id.?) += value

    value.id.fold(created)(_ => nothing)
  }

  def update(value: M): DBIO[Option[Affected]] = {

    val nothing: DBIO[Option[Affected]] = DBIO.successful(None)

    val updated: Id => DBIO[Option[Affected]] = (id: Id) =>
      entities
        .filter(_.id === id)
        .update(value)
        .map(Some.apply)

    value.id.fold(nothing)(updated)
  }

  def readAll(): DBIO[Seq[M]] = entities.result

  def readById(id: Id): DBIO[Option[M]] = entities.filter(_.id === id).result.headOption

  def deleteById(id: Id): DBIO[Affected] = entities.filter(_.id === id).delete

}
