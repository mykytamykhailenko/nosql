package com.aimprosoft.mat

import scala.concurrent.Future

trait Materializer[F[_]] {

  def materialize[M](v: F[M]): Future[M]

}

object Materializer {

  val futureMaterializer: Materializer[Future] = new Materializer[Future] {
    override def materialize[M](v: Future[M]): Future[M] = v
  }

}
