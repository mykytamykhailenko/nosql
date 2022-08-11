package util.inter

import cats.data.State
import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.common.lang.BasicActionLang
import com.aimprosoft.common.model._

object BasicOpStateInterp {

  implicit class BoolOps(v: Boolean) {

    def toAffected: Affected = if (v) 1 else 0

  }

  case class StateActionLang[M <: TIdentity]() extends BasicActionLang[λ[R => State[Map[Id, M], R]], M] {

    def create(value: M): State[Map[Id, M], Option[Id]] = State { v =>

      def absent = (v + (value.hashCode -> value)) -> value.id

      def present = v -> None

      value.id.fold(absent)(_ => present)
    }

    def update(value: M): State[Map[Id, M], Option[Affected]] = State { v =>

      val updatedState = value.id.fold(v)(v.updated(_, value))

      val affectedState = value.id.fold(none[Affected])(id => v.contains(id).toAffected.some)

      updatedState -> affectedState
    }

    def readAll: State[Map[Id, M], Seq[M]] = State(v => v -> v.values.toSeq)

    def readById(id: Id): State[Map[Id, M], Option[M]] = State(v => v -> v.get(id))

    def deleteById(id: Id): State[Map[Id, M], Affected] = State { v =>

      val reduced = v - id

      reduced -> (reduced.size < v.size).toAffected
    }

  }

  val employeeSlickInterp: BasicActionLang[λ[R => State[Map[Id, Employee], R]], Employee] = StateActionLang[Employee]()

  val departmentSlickInterp: BasicActionLang[λ[R => State[Map[Id, Department], R]], Department] = StateActionLang[Department]()

}
