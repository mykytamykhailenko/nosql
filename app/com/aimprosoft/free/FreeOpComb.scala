package com.aimprosoft.free

import cats.InjectK
import cats.free.Free
import com.aimprosoft.free.grammar._
import com.aimprosoft.model._

/**
 * Contains smart constructors for [[cats.free.Free Free]], which can be cast to [[cats.data.EitherK EitherK]], which can be used to combine grammars.
 *
 * @param I Instance of [[cats.InjectK]]
 * @tparam F [[cats.data.EitherK EitherK]], which combines two grammars
 * @tparam M Model type
 */
// Previously, I had to use path-dependent types to fix the model type.
// It caused some problems, so I switched to type lambdas with kind-projector.
class FreeOpComb[F[_], M <: TIdentity](implicit I: InjectK[Op[M, *], F]) {

    def create(v: M)(implicit I: InjectK[Op[M, *], F]): Free[F, Option[Id]] = Free.liftInject[F](CreateOp(v): Op[M, Option[Id]])

    def update(v: M)(implicit I: InjectK[Op[M, *], F]): Free[F, Option[Affected]] = Free.liftInject[F](UpdateOp(v): Op[M, Option[Affected]])

    def readAll()(implicit I: InjectK[Op[M, *], F]): Free[F, Seq[M]] = Free.liftInject[F](ReadAllOp(): Op[M, Seq[M]])

    def readById(id: Id)(implicit I: InjectK[Op[M, *], F]): Free[F, Option[M]] = Free.liftInject[F](ReadByIdOp(id): Op[M, Option[M]])

    def deleteById(id: Id)(implicit I: InjectK[Op[M, *], F]): Free[F, Affected] = Free.liftInject[F](DeleteByIdOp(id): Op[M, Affected])

}

object FreeOpComb {

  def apply[F[_], M <: TIdentity](implicit I: InjectK[Op[M, *], F]): FreeOpComb[F, M] = new FreeOpComb[F, M]

}
