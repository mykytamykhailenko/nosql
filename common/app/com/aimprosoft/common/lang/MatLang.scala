package com.aimprosoft.common.lang

import scala.concurrent.Future

trait MatLang[F[_]] {

  def materialize[M](v: F[M]): Future[M]

}
