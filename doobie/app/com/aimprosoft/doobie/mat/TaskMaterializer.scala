package com.aimprosoft.doobie.mat

import com.aimprosoft.mat.Materializer
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future

case class TaskMaterializer(sc: Scheduler) extends Materializer[Task] {

  def materialize[M](v: Task[M]): Future[M] = v.runToFuture(sc)

}
