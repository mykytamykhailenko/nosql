package com.aimprosoft.doobie

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.aimprosoft.lang.MatLang
import com.google.inject.Inject

import scala.concurrent.Future

case class DoobieMatLang @Inject() (runtime: IORuntime) extends MatLang[IO] {

  def materialize[M](v: IO[M]): Future[M] = v.unsafeToFuture()(runtime)

}
