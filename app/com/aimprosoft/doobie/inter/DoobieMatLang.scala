package com.aimprosoft.doobie.inter

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.aimprosoft.lang.MatLang

import scala.concurrent.Future

case class DoobieMatLang(runtime: IORuntime) extends MatLang[IO] {
  def materialize[M](v: IO[M]): Future[M] = v.unsafeToFuture()(runtime)
}
