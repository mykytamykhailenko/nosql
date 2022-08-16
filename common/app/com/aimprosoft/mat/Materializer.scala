package com.aimprosoft.mat

import scala.concurrent.Future

trait Materializer[F[_]] {

  def materialize[M](v: F[M]): Future[M]

}
