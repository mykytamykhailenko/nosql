package dao

import cats.implicits.{catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model._
import dao.StateDAO.BoolOps
import util.Assign
import util.Assign.AssignOps

import scala.collection.mutable

// Can be combined, but mutable.
case class MutableStateDAO[M <: TIdentity with Product : Assign](state: mutable.Map[Id, M]) extends BasicDAO[cats.Id, M] {

  def create(value: M): cats.Id[Option[Id]] = value.id.fold {
    val created = value.assignId
    state += created.id.get -> created
    created.id
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


