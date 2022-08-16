package inter

import cats.data.State
import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.common.lang.BasicDAO
import com.aimprosoft.common.model._

import scala.collection.mutable

object BasicOpStateInterp {

  def employeeAssigner(id: Id, employee: Employee): Employee = employee.copy(id = Some(id))

  def departmentAssigner(id: Id, department: Department): Department = department.copy(id = Some(id))

  def assign[M](model: M, f: (Id, M) => M): M = f(model.hashCode, model)

  implicit class BoolOps(v: Boolean) {

    def toAffected: Affected = if (v) 1 else 0

  }

  // Very functional, but cannot be combined.
  case class StateActionLang[M <: TIdentity](assigner: (Id, M) => M) extends BasicDAO[λ[R => State[Map[Id, M], R]], M] {

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

  // Can be combined, but mutable.
  case class MutableStateActionLang[M <: TIdentity](state: mutable.Map[Id, M], assigner: (Id, M) => M) extends BasicDAO[cats.Id, M] {

    def create(value: M): cats.Id[Option[Id]] = value.id.fold {
      val id = value.hashCode
      state += id -> assigner(id, value)
      id.some
    }(_ => none[Id])


    def update(value: M): cats.Id[Option[Affected]] = value.id.fold(none[Affected]) { id =>

      val present = state.contains(id)
      if (present) state.update(id, value)

      present.toAffected.some
    }

    def readAll(): cats.Id[Seq[M]] = state.values.toSeq

    def readById(id: Id): cats.Id[Option[M]] = state.get(id)

    def deleteById(id: Id): cats.Id[Affected] = {
      val present = state.contains(id)

      state -= id
      present.toAffected
    }

  }

}
