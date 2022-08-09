package com.aimprosoft.slick.free

import cats.data.State
import cats.~>
import com.aimprosoft.Util.{boolToAffected, boolToAffectedOpt}
import com.aimprosoft.free.grammar._
import com.aimprosoft.model._

import scala.reflect.ClassTag

object OpStateInterp {

  // As you may have noticed, λ[α => State[Map[Id, M], α]] looks too complex. And it is.
  // State[Map[Id, M], *]] would look much better, but unfortunately, it does not work.
  def composeOpStateInterp[M <: TIdentity : ClassTag]: Op[M, *] ~> λ[α => State[Map[Id, M], α]] = new (Op[M, *] ~> λ[α => State[Map[Id, M], α]]) {

    def apply[A](fa: Op[M, A]): State[Map[Id, M], A] = fa match {
      case CreateOp(value: M) =>
        State { v =>

          def absent: (Map[Id, M], A) = (v + (value.hashCode -> value)) -> value.id.asInstanceOf[A]

          def present: (Map[Id, M], A) = v -> None.asInstanceOf[A]

          value.id.fold(absent)(_ => present)
        }
      case UpdateOp(value: M) =>
        State { v =>

          // Updates a record by its id.
          val updatedState = value.id.fold(v)(v.updated(_, value))

          val unaffected = (None: Option[Affected]).asInstanceOf[A]
          val affected = (id: Id) => boolToAffectedOpt(v.contains(id)).asInstanceOf[A]

          val affectedState = value.id.fold(unaffected)(affected)

          updatedState -> affectedState
        }
      case ReadAllOp() => State(v => v -> v.values.toSeq.asInstanceOf[A])

      case ReadByIdOp(id: Id) => State(v => v -> v.get(id).asInstanceOf[A])

      case DeleteByIdOp(id: Id) => State { v =>
        val reduced = v - id
        reduced -> boolToAffected(reduced.size < v.size).asInstanceOf[A]
      }
    }
  }

  // This cannot be combined because of the target type being different (map of employees vs. map of departments).
  // You can overcome this by using a mutable map under hood and use Id[A].
  implicit val employeeStateInterp: Op[Employee, *] ~> State[Map[Id, Employee], *] = composeOpStateInterp[Employee]

  implicit val departmentStateInterp: Op[Department, *] ~> State[Map[Id, Department], *] = composeOpStateInterp[Department]


}
