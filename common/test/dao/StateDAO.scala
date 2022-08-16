package dao

import cats.data.State
import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import dao.StateDAO.{BoolOps, assign}

object StateDAO {

  def employeeAssigner(id: Id, employee: Employee): Employee = employee.copy(id = Some(id))

  def departmentAssigner(id: Id, department: Department): Department = department.copy(id = Some(id))

  def assign[M](model: M, f: (Id, M) => M): M = f(model.hashCode, model)

  implicit class BoolOps(v: Boolean) {

    def toAffected: Affected = if (v) 1 else 0

  }
}

// Very functional, but cannot be combined.
case class StateDAO[M <: TIdentity](assigner: (Id, M) => M) extends BasicDAO[λ[R => State[Map[Id, M], R]], M] {

  def create(value: M): State[Map[Id, M], Option[Id]] = State { v =>

    val created = assign(value, assigner)

    def absent = (v + (created.id.get -> created)) -> created.id

    def present = v -> None

    value.id.fold(absent)(_ => present)
  }

  def update(value: M): State[Map[Id, M], Option[Affected]] = State { v =>

    val updatedState = value.id.fold(v)(v.updated(_, value))

    val affectedState = value.id.fold(none[Affected])(id => v.contains(id).toAffected.some)

    updatedState -> affectedState
  }

  def readAll(): State[Map[Id, M], Seq[M]] = State(v => v -> v.values.toSeq)

  def readById(id: Id): State[Map[Id, M], Option[M]] = State(v => v -> v.get(id))

  def deleteById(id: Id): State[Map[Id, M], Affected] = State { v =>

    val reduced = v - id

    reduced -> (reduced.size < v.size).toAffected
  }

}