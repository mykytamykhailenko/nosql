package com.aimprosoft.doobie.inter

import cats.effect.IO
import com.aimprosoft.common.lang.MatLang

import scala.concurrent.Future

case class IOMatLang() extends MatLang[IO] {

  def materialize[M](v: IO[M]): Future[M] = v.unsafeToFuture()

}
