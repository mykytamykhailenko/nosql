package com.aimprosoft.slick.inter

import com.aimprosoft.model._
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.table.{DepartmentTable, EmployeeTable, TSlickBaseTable}
import com.aimprosoft.lang.BasicActionLang

import scala.concurrent.ExecutionContext

object BasicOpSlickInterp {

  trait SlickActionLang[M <: TIdentity, T <: Table[M] with TSlickBaseTable[M]] extends BasicActionLang[DBIO, M] {

    implicit val ec: ExecutionContext

    val entities: TableQuery[T]

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

    def readAll: DBIO[Seq[M]] = entities.result

    def readById(id: Id): DBIO[Option[M]] = entities.filter(_.id === id).result.headOption

    def deleteById(id: Id): DBIO[Affected] = entities.filter(_.id === id).delete

  }

  def composeSlickInterp[M <: TIdentity, T <: Table[M] with TSlickBaseTable[M]](table: TableQuery[T])(implicit cont: ExecutionContext): BasicActionLang[DBIO, M] = new SlickActionLang[M, T] {

    implicit val ec: ExecutionContext = cont

    val entities: TableQuery[T] = table

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  val employeeSlickInterp: BasicActionLang[DBIO, Employee] = composeSlickInterp(TableQuery[EmployeeTable])

  val departmentSlickInterp: BasicActionLang[DBIO, Department] = composeSlickInterp(TableQuery[DepartmentTable])


}
