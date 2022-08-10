package com.aimprosoft.slick.inter

import cats.data.State
import cats.implicits.none
import com.aimprosoft.Util.{boolToAffected, boolToAffectedOpt}
import com.aimprosoft.model._
import com.aimprosoft.lang.BasicActionLang

// Will be a part of test utils.
object BasicOpStateInterp {

  case class StateActionLang[M <: TIdentity]() extends BasicActionLang[λ[R => State[Map[Id, M], R]], M] {

    def create(value: M): State[Map[Id, M], Option[Id]] = State { v =>

      def absent = (v + (value.hashCode -> value)) -> value.id

      def present = v -> None

      value.id.fold(absent)(_ => present)
    }

    def update(value: M): State[Map[Id, M], Option[Affected]] = State { v =>

      val updatedState = value.id.fold(v)(v.updated(_, value))

      val affectedState = value.id.fold(none[Affected])(id => boolToAffectedOpt(v.contains(id)))

      updatedState -> affectedState
    }

    def readAll: State[Map[Id, M], Seq[M]] = State(v => v -> v.values.toSeq)

    def readById(id: Id): State[Map[Id, M], Option[M]] = State(v => v -> v.get(id))

    def deleteById(id: Id): State[Map[Id, M], Affected] = State { v =>

      val reduced = v - id

      reduced -> boolToAffected(reduced.size < v.size)
    }

  }

  val employeeSlickInterp: BasicActionLang[λ[R => State[Map[Id, Employee], R]], Employee] = StateActionLang[Employee]()

  val departmentSlickInterp: BasicActionLang[λ[R => State[Map[Id, Department], R]], Department] = StateActionLang[Department]()

}
