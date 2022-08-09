package com.aimprosoft.slick.inter

import cats.data.State
import cats.implicits.none
import com.aimprosoft.Util.{boolToAffected, boolToAffectedOpt}
import com.aimprosoft.model._
import com.aimprosoft.lang.BasicActionLang

object BasicOpStateInterp {

  trait StateActionLang[M <: TIdentity] extends BasicActionLang[λ[α => State[Map[Id, M], α]], M] {

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

  val employeeSlickInterp: BasicActionLang[λ[α => State[Map[Id, Employee], α]], Employee] = new StateActionLang[Employee] { }

  val departmentSlickInterp: BasicActionLang[λ[α => State[Map[Id, Department], α]], Department] = new StateActionLang[Department] {}

}
