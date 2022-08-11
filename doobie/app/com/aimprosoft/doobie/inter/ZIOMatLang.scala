package com.aimprosoft.doobie.inter

import com.aimprosoft.common.lang.MatLang
import zio.{Task, Unsafe}

import scala.concurrent.Future

import zio.Runtime

case class ZIOMatLang[R](runtime: Runtime[R]) extends MatLang[Task] {

  def materialize[M](v: Task[M]): Future[M] = Unsafe.unsafe { implicit unsafe =>
    runtime.unsafe.runToFuture(v)
  }

}
