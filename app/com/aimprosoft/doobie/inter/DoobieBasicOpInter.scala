package com.aimprosoft.doobie.inter

import cats.effect._
import cats.implicits._
import com.aimprosoft.lang.BasicActionLang
import com.aimprosoft.model._
import doobie.Fragment.const
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Read => DoobieRead, Write => DoobieWrite}


object DoobieBasicOpInter {

  case class DoobieActionLang[F[_] : Async, M <: TIdentity : DoobieWrite : DoobieRead]()(implicit aux: Aux[F, Unit], gtf: GetFields[M]) extends BasicActionLang[F, M] {

    def create(value: M): F[Option[Id]] = {

      val insertFields: String = gtf.fieldsWithoutId.mkString("(", ", ", ")")

      val insert =
        (fr"insert into" ++ const(gtf.table) ++ const(insertFields) ++ fr"values ($value)")
          .update
          .withGeneratedKeys[Id](gtf.id)
          .compile
          .last
          .transact(aux)

      value.id.fold(insert)(_ => Async[F].pure(none[Id]))
    }

    def update(value: M): F[Option[Affected]] =
      value.id.fold(Async[F].pure(none[Id])) { id =>

        val fieldsAndValues = gtf.getFieldMap(value) - gtf.id

        val fields = fieldsAndValues.map { case (field, value) => const(field) ++ fr" = $value" }

        val setStatement = fr"set" ++ fields.reduce(_ ++ fr"," ++ _)

        val updateStmt = fr"update" ++ const(gtf.table) ++ setStatement ++ fr"where" ++ const(gtf.id) ++ fr"= $id"
        val rowCountStmt = sql"select row_count()"

        val ops = for {
          _ <- updateStmt.update.run
          affected <- rowCountStmt.query[Affected].option
        } yield affected

        ops.transact(aux)
      }

    def readAll: F[Seq[M]] = (fr"select * from" ++ const(gtf.table)).query[M].to[Seq].transact(aux)

    def readById(id: Id): F[Option[M]] = (fr"select * from" ++ const(gtf.table) ++ fr"where" ++ const(gtf.id) ++ fr"= $id").query[M].option.transact(aux)

    def deleteById(id: Id): F[Affected] = {
      val deleteStmt = (fr"delete from" ++ const(gtf.table) ++ fr"where" ++ const(gtf.id) ++ fr"= $id")
      val rowCountStmt = sql"select row_count()"

      val deletion = for {
        _ <- deleteStmt.update.run
        affected <- rowCountStmt.query[Affected].unique
      } yield affected

      deletion.transact(aux)
    }


  }

}
