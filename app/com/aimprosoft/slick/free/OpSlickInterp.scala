package com.aimprosoft.slick.free

import cats.data.EitherK
import cats.~>
import com.aimprosoft.free.grammar.{Op, _}
import com.aimprosoft.model._

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.table.{DepartmentTable, EmployeeTable, TSlickBaseTable}

object OpSlickInterp {

  def composeOpSlickInterp[M <: TIdentity : ClassTag, T <: Table[M] with TSlickBaseTable[M]](entities: TableQuery[T])(implicit ec: ExecutionContext): Op[M, *] ~> DBIO[*] = new (Op[M, *] ~> DBIO[*]) {

    def apply[A](fa: Op[M, A]): DBIO[A] = fa match {

      case CreateOp(value: M) =>

        val nothing: DBIO[Option[Id]] = DBIO.successful(None)
        val created: DBIO[Option[Id]] = entities returning entities.map(_.id.?) += value

        value.id.fold(created)(_ => nothing).asInstanceOf[DBIO[A]]

      case UpdateOp(value: M) =>

        val nothing: DBIO[Option[Affected]] = DBIO.successful(None)

        val updated: Id => DBIO[Option[Affected]] = (id: Id) =>
          entities
            .filter(_.id === id)
            .update(value)
            .map(Some.apply)

        value.id.fold(nothing)(updated).asInstanceOf[DBIO[A]]

      case ReadAllOp() => entities.result.asInstanceOf[DBIO[A]]

      case ReadByIdOp(id: Id) => entities.filter(_.id === id).result.headOption.asInstanceOf[DBIO[A]]

      case DeleteByIdOp(id: Id) => entities.filter(_.id === id).delete.asInstanceOf[DBIO[A]]
    }

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  val departmentSlickInterp: Op[Department, *] ~> DBIO = composeOpSlickInterp(TableQuery[DepartmentTable])

  val employeeSlickInterp: Op[Employee, *] ~> DBIO = composeOpSlickInterp(TableQuery[EmployeeTable])

  val slickInterp: EitherK[Op[Department, *], Op[Employee, *], *] ~> DBIO = departmentSlickInterp.or(employeeSlickInterp)

}
