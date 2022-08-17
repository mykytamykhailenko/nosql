package dao

import cats.data.State
import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import dao.StateDAO.BoolOps
import util.Assign
import util.Assign.AssignOps


object StateDAO {

  implicit class BoolOps(v: Boolean) {

    def toAffected: Affected = if (v) 1 else 0

  }
}

// Very functional, but cannot be combined.
case class StateDAO[M <: TIdentity with Product : Assign]() extends BasicDAO[λ[R => State[Map[Id, M], R]], M] {

  def create(value: M): State[Map[Id, M], Option[Id]] = State { v =>

    val created = value.assignId

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
      v - id -> v.contains(id).toAffected
  }

}