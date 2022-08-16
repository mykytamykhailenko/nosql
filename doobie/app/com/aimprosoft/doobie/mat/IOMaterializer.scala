package com.aimprosoft.doobie.mat

import cats.effect.IO
import com.aimprosoft.mat.Materializer

import scala.concurrent.Future

case class IOMaterializer() extends Materializer[IO] {

  def materialize[M](v: IO[M]): Future[M] = v.unsafeToFuture()

}
