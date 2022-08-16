package com.aimprosoft.doobie.dao

import cats.effect.Async
import cats.implicits.none
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.doobie.helpers.GetFields
import com.aimprosoft.model.{Affected, Id, TIdentity}
import doobie.implicits._
import doobie.util.Read
import doobie.util.fragment.Fragment.const
import doobie.util.transactor.Transactor.Aux

case class DoobieDAO[F[_] : Async, M <: TIdentity : Read]()(implicit aux: Aux[F, Unit], gtf: GetFields[M]) extends BasicDAO[F, M] {

  def create(value: M): F[Option[Id]] = {

    val insertFields: String = gtf.fieldsWithoutId.mkString("(", ", ", ")")

    val values = gtf.getFieldsAndValuesWithoutId(value).map(v => fr0"${v._2}").reduce(_ ++ fr", " ++ _)

    val insert =
      (fr"insert into" ++ const(gtf.table) ++ const(insertFields) ++
        fr0"values (" ++ values ++ fr0")")
        .update
        .withGeneratedKeys[Id](gtf.id)
        .compile
        .last
        .transact(aux)

    value.id.fold(insert)(_ => Async[F].pure(none[Id]))
  }

  def update(value: M): F[Option[Affected]] =
    value.id.fold(Async[F].pure(none[Id])) { id =>

      val fields = gtf.getFieldsAndValuesWithoutId(value).map(v => const(v._1) ++ fr"= ${v._2}")

      (fr"update" ++ const(gtf.table) ++
        fr"set" ++ fields.reduce(_ ++ fr"," ++ _) ++
        fr"where" ++ const(gtf.id) ++ fr"= $id")
        .update
        .run
        .map(Option.apply)
        .transact(aux)
    }

  def readAll(): F[Seq[M]] =
    (fr"select * from" ++ const(gtf.table))
      .query[M]
      .to[Seq]
      .transact(aux)

  def readById(id: Id): F[Option[M]] =
    (fr"select * from" ++ const(gtf.table) ++
      fr"where" ++ const(gtf.id) ++ fr"= $id")
      .query[M]
      .option
      .transact(aux)

  def deleteById(id: Id): F[Affected] =
    (fr"delete from" ++ const(gtf.table) ++
      fr"where" ++ const(gtf.id) ++ fr"= $id")
      .update
      .run
      .transact(aux)


}
