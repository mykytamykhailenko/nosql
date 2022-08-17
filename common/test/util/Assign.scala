package util

import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.model.{Department, Employee, Id, TIdentity}

trait Assign[M <: TIdentity with Product] {
  def assign(id: Id, model: M): M
}


object Assign {

  implicit class AssignOps[M <: TIdentity with Product](v: M)(implicit assigner: Assign[M]) {
    def assignId: M = assigner.assign(v.hashCode, v)
  }

  implicit val employeeAssigner: Assign[Employee] = (id, model) => model.copy(id = id.some)

  implicit val departmentAssigner: Assign[Department] = (id, model) => model.copy(id = id.some)


}