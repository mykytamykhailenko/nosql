package com.aimprosoft.doobie.inter

import cats.effect._
import cats.implicits._
import com.aimprosoft.common.lang.BasicActionLang
import com.aimprosoft.common.model._
import com.aimprosoft.doobie.helpers.GetFields
import doobie.Fragment.const
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Read => DoobieRead}

object DoobieBasicOpInter {

  case class DoobieActionLang[F[_] : Async, M <: TIdentity : DoobieRead]()(implicit aux: Aux[F, Unit], gtf: GetFields[M]) extends BasicActionLang[F, M] {

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

        val existsStmt =
          fr"select count(*) from" ++
            const(gtf.table) ++ fr"where" ++ const(gtf.id) ++ fr"= $id"

        val updateStmt =
          fr"update" ++ const(gtf.table) ++
            fr"set" ++ fields.reduce(_ ++ fr"," ++ _) ++
            fr"where" ++ const(gtf.id) ++ fr"= $id"

        val ops = for {
          _ <- updateStmt.update.run
          affected <- existsStmt.query[Affected].option
        } yield affected

        ops.transact(aux)
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

    def deleteById(id: Id): F[Affected] = {

      val existsStmt =
        fr"select count(*) from" ++
          const(gtf.table) ++ fr"where" ++ const(gtf.id) ++ fr"= $id"

      val deleteStmt =
        fr"delete from" ++ const(gtf.table) ++
          fr"where" ++ const(gtf.id) ++ fr"= $id"

      val deletion = for {
        affected <- existsStmt.query[Affected].unique
        _ <- deleteStmt.update.run
      } yield affected

      deletion.transact(aux)
    }


  }

}
