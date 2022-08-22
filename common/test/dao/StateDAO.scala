package dao

import cats.data.State
import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import dao.StateDAO.BoolOps
import util.Assign


object StateDAO {

  implicit class BoolOps(v: Boolean) {

    def toAffected: Affected = if (v) 1 else 0

  }
}

// Very functional, but cannot be combined.
case class StateDAO[K, M <: Id[K]]()(implicit as: Assign[K, M]) extends BasicDAO[λ[R => State[Map[K, M], R]], K, M] {

  def create(value: M): State[Map[K, M], Option[K]] = State { v =>

    val created = as.assign(value)

    def absent = (v + (created.id.get -> created)) -> created.id

    def present = v -> None

    value.id.fold(absent)(_ => present)
  }

  def update(value: M): State[Map[K, M], Option[Affected]] = State { v =>

    val updatedState = value.id.fold(v)(v.updated(_, value))

    val affectedState = value.id.fold(none[Affected])(id => v.contains(id).toAffected.some)

    updatedState -> affectedState
  }

  def readAll(): State[Map[K, M], Seq[M]] = State(v => v -> v.values.toSeq)

  def readById(id: K): State[Map[K, M], Option[M]] = State(v => v -> v.get(id))

  def deleteById(id: K): State[Map[K, M], Affected] = State(v => v - id -> v.contains(id).toAffected)

}