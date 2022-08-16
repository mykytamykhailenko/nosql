package com.aimprosoft.doobie.mat

import com.aimprosoft.mat.Materializer
import zio.{Runtime, Task, Unsafe}

import scala.concurrent.Future

case class ZIOMaterializer[R](runtime: Runtime[R]) extends Materializer[Task] {

  def materialize[M](v: Task[M]): Future[M] = Unsafe.unsafe { implicit unsafe =>
    runtime.unsafe.runToFuture(v)
  }

}
